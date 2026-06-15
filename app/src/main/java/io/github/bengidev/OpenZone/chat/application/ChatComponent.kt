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

        _state.update {
            it.copy(
                draft = "",
                canSend = false,
                status = ChatStreamingStatus.RUNNING,
                currentPartialText = "",
                currentPartialThinking = "",
                streamingThinkingId = null,
                streamingAnswerId = null,
                messages = it.messages + userMessage
            )
        }

        persist(snapshot.conversation, userMessage)

        val request = ChatRequest(
            conversationId = snapshot.conversation.id,
            messages = _state.value.messages,
            modelId = modelId,
            provider = provider,
            reasoningLevel = resolveReasoningLevel()
        )
        beginStream(request)
    }

    fun onStopTapped() {
        streamJob?.cancel()
        streamJob = null
        _state.update { state ->
            state.copy(
                status = ChatStreamingStatus.DONE,
                messages = state.messages.markOpenAssistantTurnsComplete()
            )
        }
    }

    fun onClearThread() {
        if (_state.value.isStreaming) return
        resetToNewConversation()
    }

    fun resetToNewConversation() {
        streamJob?.cancel()
        streamJob = null
        _state.update { ChatState() }
    }

    fun renameActiveConversation(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        _state.update { state ->
            state.copy(conversation = state.conversation.copy(title = trimmed))
        }
    }

    fun onToggleReasoning() {
        _state.update { it.copy(isReasoningExpanded = !it.isReasoningExpanded) }
    }

    // ---- Streaming -----------------------------------------------------

    private fun beginStream(request: ChatRequest) {
        streamJob?.cancel()
        streamJob = scope.launch {
            try {
                apiClient.stream(request).collect { event ->
                    applyEvent(event)
                }
            } catch (t: Throwable) {
                val err = ChatStreamError(t.message ?: "Streaming failed")
                applyEvent(ChatStreamingEvent.Error(err))
            }
        }
    }

    private fun applyEvent(event: ChatStreamingEvent) {
        when (event) {
            is ChatStreamingEvent.TextDelta -> {
                _state.update { state ->
                    val partial = state.currentPartialText + event.delta
                    val answerId = state.streamingAnswerId
                    val messages =
                            if (answerId != null) {
                                state.messages.updateTextContent(answerId, partial, isComplete = false)
                            } else {
                                val newId = newMessageId("assistant")
                                state.messages +
                                        ChatMessages.text(
                                                id = newId,
                                                role = ChatMessageRole.ASSISTANT,
                                                content = partial,
                                                isComplete = false
                                        )
                            }
                    state.copy(
                            currentPartialText = partial,
                            streamingAnswerId = answerId ?: messages.lastAssistantId(),
                            messages = messages
                    )
                }
            }
            is ChatStreamingEvent.ThinkingDelta -> {
                _state.update { state ->
                    val partial = state.currentPartialThinking + event.delta
                    if (partial.trim().isEmpty()) {
                        return@update state.copy(currentPartialThinking = partial)
                    }
                    val thinkingId = state.streamingThinkingId
                    val messages =
                            if (thinkingId != null) {
                                state.messages.updateThinkingContent(
                                        thinkingId,
                                        partial,
                                        isComplete = false
                                )
                            } else {
                                val newId = newMessageId("thinking")
                                state.messages +
                                        ChatMessages.thinking(
                                                id = newId,
                                                content = partial,
                                                isComplete = false
                                        )
                            }
                    state.copy(
                            currentPartialThinking = partial,
                            streamingThinkingId = thinkingId ?: messages.lastThinkingId(),
                            messages = messages
                    )
                }
            }
            ChatStreamingEvent.Done -> {
                val assistantId = _state.value.streamingAnswerId
                _state.update { state ->
                    var messages = state.messages
                    state.streamingThinkingId?.let { id ->
                        messages = messages.markThinkingComplete(id)
                    }
                    state.streamingAnswerId?.let { id ->
                        messages = messages.markTextComplete(id)
                    }
                    state.copy(
                            status = ChatStreamingStatus.DONE,
                            currentPartialText = "",
                            currentPartialThinking = "",
                            streamingThinkingId = null,
                            streamingAnswerId = null,
                            messages = messages
                    )
                }
                assistantId?.let(::persistCompletedAssistant)
                streamJob = null
            }
            is ChatStreamingEvent.Error -> {
                _state.update { state ->
                    state.copy(
                            status = ChatStreamingStatus.FAILED,
                            currentPartialText = "",
                            currentPartialThinking = "",
                            streamingThinkingId = null,
                            streamingAnswerId = null,
                            messages = state.messages
                                    .markOpenAssistantTurnsComplete()
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

    /** Sets absolute `content` on the text message matching `id`. */
    private fun List<ChatMessage>.updateTextContent(
        id: String,
        content: String,
        isComplete: Boolean
    ): List<ChatMessage> =
        map { message ->
            if (message is ChatMessage.Text && message.id == id) {
                ChatMessage.Text(message.message.copy(content = content, isComplete = isComplete))
            } else {
                message
            }
        }

    /** Sets absolute `content` on the reasoning message matching `id`. */
    private fun List<ChatMessage>.updateThinkingContent(
        id: String,
        content: String,
        isComplete: Boolean
    ): List<ChatMessage> =
        map { message ->
            if (message is ChatMessage.Thinking && message.id == id) {
                ChatMessage.Thinking(message.message.copy(content = content, isComplete = isComplete))
            } else {
                message
            }
        }

    private fun List<ChatMessage>.markTextComplete(id: String): List<ChatMessage> =
        map { message ->
            if (message is ChatMessage.Text && message.id == id) {
                ChatMessage.Text(message.message.copy(isComplete = true))
            } else {
                message
            }
        }

    private fun List<ChatMessage>.markThinkingComplete(id: String): List<ChatMessage> =
        map { message ->
            if (message is ChatMessage.Thinking && message.id == id) {
                ChatMessage.Thinking(message.message.copy(isComplete = true))
            } else {
                message
            }
        }

    private fun List<ChatMessage>.lastAssistantId(): String? =
        lastOrNull { it is ChatMessage.Text && it.message.role == ChatMessageRole.ASSISTANT }
            ?.let { (it as ChatMessage.Text).id }

    private fun List<ChatMessage>.lastThinkingId(): String? =
        lastOrNull { it is ChatMessage.Thinking }?.let { (it as ChatMessage.Thinking).id }

    /** Marks any in-flight assistant turn rows complete on failure/cancel. */
    private fun List<ChatMessage>.markOpenAssistantTurnsComplete(): List<ChatMessage> {
        val lastAssistantId = lastAssistantId()
        val lastThinkingId = lastThinkingId()
        return map { message ->
            when (message) {
                is ChatMessage.Text ->
                    if (message.id == lastAssistantId) {
                        ChatMessage.Text(message.message.copy(isComplete = true))
                    } else {
                        message
                    }
                is ChatMessage.Thinking ->
                    if (message.id == lastThinkingId) {
                        ChatMessage.Thinking(message.message.copy(isComplete = true))
                    } else {
                        message
                    }
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
