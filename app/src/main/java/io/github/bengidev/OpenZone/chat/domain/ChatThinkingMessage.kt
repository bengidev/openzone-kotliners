package io.github.bengidev.openzone.chat.domain

/**
 * Reasoning/thinking message — model chain-of-thought streamed separately
 * from the answer, then collapsed/hidden once the turn completes.
 * Mirrors iOS `ChatThinkingMessage`.
 */
data class ChatThinkingMessage(
    val id: String,
    val role: ChatMessageRole = ChatMessageRole.ASSISTANT,
    val content: String,
    val isComplete: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
