package io.github.bengidev.openzone.chat.domain

/**
 * Request payload sent to a chat API client.
 * Mirrors iOS `ChatRequest`.
 */
data class ChatRequest(
    val conversationId: String,
    val messages: List<ChatMessage>,
    val modelId: String
) {
    val latestUserText: String
        get() = latestUserTextIn(messages)

    companion object {
        fun latestUserTextIn(messages: List<ChatMessage>): String =
            messages.reversed()
                .firstOrNull { it is ChatMessage.Text && it.message.role == ChatMessageRole.USER }
                ?.let { (it as ChatMessage.Text).message.content }
                .orEmpty()
    }
}
