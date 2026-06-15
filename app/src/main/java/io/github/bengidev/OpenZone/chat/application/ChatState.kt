package io.github.bengidev.openzone.chat.application

import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus

/**
 * Chat feature state, observed by the presenter via Compose snapshot flow.
 * Mirrors iOS `ChatFeature.State`.
 */
data class ChatState(
    val conversation: ChatConversation = defaultConversation(),
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val status: ChatStreamingStatus = ChatStreamingStatus.IDLE,
    val isReasoningExpanded: Boolean = false,
    val canSend: Boolean = false,
    /** Accumulated assistant answer text for the in-flight turn. */
    val currentPartialText: String = "",
    /** Accumulated reasoning text for the in-flight turn. */
    val currentPartialThinking: String = "",
    /** Stable id of the in-flight reasoning row, if one was created. */
    val streamingThinkingId: String? = null,
    /** Stable id of the in-flight assistant answer row, if one was created. */
    val streamingAnswerId: String? = null,
    /** Human-readable stream failure surfaced by the composer error banner. */
    val streamErrorMessage: String? = null
) {
    val isStreaming: Boolean
        get() = status == ChatStreamingStatus.RUNNING

    val showChatErrorBanner: Boolean
        get() = status == ChatStreamingStatus.FAILED && streamErrorMessage != null

    val hasMessages: Boolean
        get() = messages.isNotEmpty()

    /** Typing bubble while the model is running but no assistant row exists yet. */
    val showLoadingIndicator: Boolean
        get() {
            if (status != ChatStreamingStatus.RUNNING) return false
            val last = messages.lastOrNull() as? ChatMessage.Text ?: return false
            return last.message.role == ChatMessageRole.USER
        }

    companion object {
        fun defaultConversation(): ChatConversation =
            ChatConversation(
                id = "default-conversation",
                title = "New Chat"
            )
    }
}
