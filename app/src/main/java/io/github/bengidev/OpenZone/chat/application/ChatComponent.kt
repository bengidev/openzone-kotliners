package io.github.bengidev.openzone.chat.application

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamError
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus
import io.github.bengidev.openzone.chat.domain.ChatMessages
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
import io.github.bengidev.openzone.chat.infrastructure.ChatHistoryStore
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Reducer-style state holder for the chat feature.
 *
 * - Holds `ChatState` in a `MutableStateFlow` for Compose observation.
 * - Public `send(intent)` API mirrors iOS `ChatFeature.Action` cases.
 * - Streams chat deltas from a `ChatAPIClient` and merges them into the
 *   active assistant turn by stable message ID — preventing duplicate
 *   "Thinking" rows when a late reasoning chunk arrives.
 *
 * Mirrors iOS `ChatFeature` (TCA reducer). Follows the Android Onboarding
 * pattern (`OnboardingComponent`) of explicit `send()` / `state` API rather
 * than deep TCA-style stores.
 *
 * The provider+model that a send targets are resolved lazily via
 * [resolveProvider] / [resolveModelId] (filled from the preference store by the
 * owner) so this component stays free of persistence and Android dependencies
 * and remains unit-testable with a canned [ChatAPIClient]. A send is hard-blocked
 * unless [canStartSend] reports a stored credential and a selected model.
 */
class ChatComponent(
    private val apiClient: ChatAPIClient,
    private val scope: CoroutineScope,
    private val resolveProvider: () -> ChatProvider = { ChatProviders.openRouter },
    private val resolveModelId: () -> String? = { null },
    private val resolveReasoningLevel: () -> ComposerReasoningLevel = { ComposerReasoningLevel.Off },
    private val canStartSend: () -> Boolean = { true },
    private val historyStore: ChatHistoryStore? = null,
    initialState: ChatState = ChatState()
) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var streamJob: Job? = null

    // ---- History restore ------------------------------------------------

    /**
     * Restores the active conversation's persisted messages from
     * [historyStore], replacing the current (empty) message list. Safe to call
     * once at startup; a no-op when no store is wired or no history exists.
     * Never clobbers an in-flight stream.
     */
    fun restoreHistory() {
        val store = historyStore ?: return
        scope.launch {
            val conversationId = _state.value.conversation.id
            val restored = store.loadMessages(conversationId)
            if (restored.isEmpty()) return@launch
            _state.update { state ->
                if (state.isStreaming || state.messages.isNotEmpty()) state
                else state.copy(messages = restored)
            }
        }
    }

    /**
     * Reopens a persisted [conversation] (issue #8 sidebar): cancels any in-flight
     * stream, switches the active conversation so continued sends persist into it,
     * and replaces the thread with that conversation's stored messages. A no-op
     * when no store is wired. Selecting the already-active conversation still
     * re-loads it, which is harmless (idempotent overwrite of identical state).
     */
    fun openConversation(conversation: io.github.bengidev.openzone.chat.domain.ChatConversation) {
        val store = historyStore ?: return
        streamJob?.cancel()
        streamJob = null
        scope.launch {
            val restored = store.loadMessages(conversation.id)
            _state.update { state ->
                state.copy(
                    conversation = conversation,
                    messages = restored,
                    draft = "",
                    canSend = false,
                    status = ChatStreamingStatus.IDLE,
                    isReasoningExpanded = false
                )
            }
        }
    }

    // ---- Intents (mirroring iOS ChatFeature.Action) ---------------------

    fun onDraftChanged(text: String) {
        _state.update { it.copy(draft = text, canSend = text.isNotBlank() && !it.isStreaming) }
    }

    fun onSendTapped() {
        val snapshot = _state.value
        val draft = snapshot.draft.trim()
        if (draft.isEmpty() || snapshot.isStreaming) return

        // Hard-block: require a stored credential and a selected model before
        // a send can start. The owner derives this from the credential +
        // preference stores; without it the send path is a no-op.
        val modelId = resolveModelId()
        if (!canStartSend() || modelId.isNullOrBlank()) return
        val provider = resolveProvider()

        val userMessage = ChatMessages.text(
            id = newMessageId("user"),
            role = ChatMessageRole.USER,
            content = draft,
            isComplete = true
        )
        val thinkingMessage = ChatMessages.thinking(
            id = newMessageId("thinking"),
            content = "",
            isComplete = false
        )
        val assistantMessage = ChatMessages.text(
            id = newMessageId("assistant"),
            role = ChatMessageRole.ASSISTANT,
            content = "",
            isComplete = false
        )

        _state.update {
            it.copy(
                draft = "",
                canSend = false,
                status = ChatStreamingStatus.RUNNING,
                messages = it.messages + listOf(userMessage, thinkingMessage, assistantMessage)
            )
        }

        // Turn-boundary write #1: persist the user message immediately on send.
        // An errored or killed turn therefore loses only in-flight assistant
        // text, never the user's message.
        persist(snapshot.conversation, userMessage)

        val request = ChatRequest(
            conversationId = snapshot.conversation.id,
            messages = _state.value.messages,
            modelId = modelId,
            provider = provider,
            reasoningLevel = resolveReasoningLevel()
        )
        beginStream(request, thinkingMessage.id, assistantMessage.id)
    }

    fun onStopTapped() {
        streamJob?.cancel()
        streamJob = null
        _state.update { state ->
            state.copy(
                status = ChatStreamingStatus.DONE,
                messages = state.messages.markAssistantTurnsComplete()
            )
        }
    }

    fun onClearThread() {
        if (_state.value.isStreaming) return
        _state.update { it.copy(messages = emptyList(), isReasoningExpanded = false) }
    }

    fun onToggleReasoning() {
        _state.update { it.copy(isReasoningExpanded = !it.isReasoningExpanded) }
    }

    // ---- Streaming -----------------------------------------------------

    private fun beginStream(
        request: ChatRequest,
        thinkingId: String,
        assistantId: String
    ) {
        streamJob?.cancel()
        streamJob = scope.launch {
            try {
                apiClient.stream(request).collect { event ->
                    applyEvent(event, thinkingId, assistantId)
                }
            } catch (t: Throwable) {
                val err = ChatStreamError(t.message ?: "Streaming failed")
                applyEvent(ChatStreamingEvent.Error(err), thinkingId, assistantId)
            }
        }
    }

    private fun applyEvent(
        event: ChatStreamingEvent,
        thinkingId: String,
        assistantId: String
    ) {
        when (event) {
            is ChatStreamingEvent.TextDelta -> {
                _state.update { state ->
                    state.copy(
                        messages = state.messages.appendTextDelta(assistantId, event.delta)
                    )
                }
            }
            is ChatStreamingEvent.ThinkingDelta -> {
                _state.update { state ->
                    state.copy(
                        messages = state.messages.appendThinkingDelta(thinkingId, event.delta)
                    )
                }
            }
            ChatStreamingEvent.Done -> {
                _state.update { state ->
                    state.copy(
                        status = ChatStreamingStatus.DONE,
                        messages = state.messages.markAssistantTurnsComplete()
                    )
                }
                // Turn-boundary write #2: persist the completed assistant text
                // once, after the stream finishes. No per-delta writes.
                persistCompletedAssistant(assistantId)
                streamJob = null
            }
            is ChatStreamingEvent.Error -> {
                _state.update { state ->
                    state.copy(
                        status = ChatStreamingStatus.FAILED,
                        messages = state.messages
                            .markAssistantTurnsComplete()
                            .appendSystemError(event.error.message)
                    )
                }
                streamJob = null
            }
        }
    }

    // ---- Helpers -------------------------------------------------------

    private fun newMessageId(prefix: String): String = "$prefix-${UUID.randomUUID()}"

    /** Persists a single message at a turn boundary (fire-and-forget on [scope]). */
    private fun persist(
        conversation: io.github.bengidev.openzone.chat.domain.ChatConversation,
        message: ChatMessage
    ) {
        val store = historyStore ?: return
        scope.launch {
            store.upsertConversation(conversation)
            store.upsertMessage(conversation.id, message)
        }
    }

    /**
     * Persists the completed assistant text row identified by [assistantId] once,
     * on stream completion. Looks the row up from current state so the stored
     * copy carries the fully-streamed content and `isComplete = true`.
     */
    private fun persistCompletedAssistant(assistantId: String) {
        if (historyStore == null) return
        val state = _state.value
        val assistant = state.messages.firstOrNull {
            it is ChatMessage.Text && it.id == assistantId
        } ?: return
        persist(state.conversation, assistant)
    }

    /** Appends `delta` to a text message matching `id` (user or assistant). */
    private fun List<ChatMessage>.appendTextDelta(id: String, delta: String): List<ChatMessage> =
        map { message ->
            if (message is ChatMessage.Text && message.id == id) {
                ChatMessage.Text(message.message.copy(content = message.message.content + delta))
            } else message
        }

    /** Appends `delta` to the reasoning message matching `id`. */
    private fun List<ChatMessage>.appendThinkingDelta(id: String, delta: String): List<ChatMessage> =
        map { message ->
            if (message is ChatMessage.Thinking && message.id == id) {
                ChatMessage.Thinking(message.message.copy(content = message.message.content + delta))
            } else message
        }

    /** Marks the latest open thinking + assistant text turn as complete. */
    private fun List<ChatMessage>.markAssistantTurnsComplete(): List<ChatMessage> {
        val lastAssistantId = indexOfLast {
            it is ChatMessage.Text && it.message.role == ChatMessageRole.ASSISTANT
        }.takeIf { it != -1 }?.let { (this[it] as ChatMessage.Text).id }
        val lastThinkingId = indexOfLast { it is ChatMessage.Thinking }
            .takeIf { it != -1 }?.let { (this[it] as ChatMessage.Thinking).id }

        return map { message ->
            when (message) {
                is ChatMessage.Text ->
                    if (message.id == lastAssistantId) {
                        ChatMessage.Text(message.message.copy(isComplete = true))
                    } else message
                is ChatMessage.Thinking ->
                    if (message.id == lastThinkingId) {
                        ChatMessage.Thinking(message.message.copy(isComplete = true))
                    } else message
                is ChatMessage.System -> message
            }
        }
    }

    /** Appends a system message (used for error surfaces). */
    private fun List<ChatMessage>.appendSystemError(message: String): List<ChatMessage> =
        this + ChatMessages.system(
            id = "system-${UUID.randomUUID()}",
            content = message
        )
}
