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
    val canSend: Boolean = false
) {
    val isStreaming: Boolean
        get() = status == ChatStreamingStatus.RUNNING

    val hasMessages: Boolean
        get() = messages.isNotEmpty()

    companion object {
        fun defaultConversation(): ChatConversation =
            ChatConversation(
                id = "default-conversation",
                title = "New Chat"
            )
    }
}
