package io.github.bengidev.openzone.chat.domain

/**
 * Plain text message (user or assistant turn).
 * `isComplete` flips true when the turn finishes streaming.
 * Mirrors iOS `ChatTextMessage`.
 */
data class ChatTextMessage(
    val id: String,
    val role: ChatMessageRole,
    val content: String,
    val isComplete: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
