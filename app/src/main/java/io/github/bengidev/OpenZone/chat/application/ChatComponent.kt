package io.github.bengidev.openzone.chat.application

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamError
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus
import io.github.bengidev.openzone.chat.domain.ChatMessages
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
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
 */
class ChatComponent(
    private val apiClient: ChatAPIClient,
    private val scope: CoroutineScope,
    private val modelId: String = "openzone-dummy-1",
    initialState: ChatState = ChatState()
) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var streamJob: Job? = null

    // ---- Intents (mirroring iOS ChatFeature.Action) ---------------------

    fun onDraftChanged(text: String) {
        _state.update { it.copy(draft = text, canSend = text.isNotBlank() && !it.isStreaming) }
    }

    fun onSendTapped() {
        val snapshot = _state.value
        val draft = snapshot.draft.trim()
        if (draft.isEmpty() || snapshot.isStreaming) return

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

        val request = ChatRequest(
            conversationId = snapshot.conversation.id,
            messages = _state.value.messages,
            modelId = modelId
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
