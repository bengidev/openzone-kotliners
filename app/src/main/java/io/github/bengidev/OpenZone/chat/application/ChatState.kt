package io.github.bengidev.openzone.chat.application

import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus

/**
 * Chat feature state, observed by the presenter via Compose snapshot flow. Mirrors iOS
 * `ChatFeature.State`.
 */
data class ChatState(
        val conversation: ChatConversation = defaultConversation(),
        val messages: List<ChatMessage> = emptyList(),
        val draft: String = "",
        val status: ChatStreamingStatus = ChatStreamingStatus.IDLE,
        val isReasoningExpanded: Boolean = false,
        val canSend: Boolean = false,
        /**
         * Stable ID of the in-flight reasoning row for the current turn. Set on the first
         * non-whitespace ThinkingDelta; nil until then. Prevents duplicate "Thinking" rows from
         * late reasoning deltas. Mirrors iOS `ChatFeature.State.streamingThinkingID`.
         */
        val streamingThinkingId: String? = null
) {
 val isStreaming: Boolean
  get() = status == ChatStreamingStatus.RUNNING

 val hasMessages: Boolean
  get() = messages.isNotEmpty()

 companion object {
  fun defaultConversation(): ChatConversation =
          ChatConversation(id = "default-conversation", title = "New Chat")
 }
}
