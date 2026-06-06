package io.github.bengidev.openzone.chat.domain

/**
 * System-level message (e.g. notifications, errors surfaced as rows).
 * Mirrors iOS `ChatSystemMessage`.
 */
data class ChatSystemMessage(
    val id: String,
    val role: ChatMessageRole = ChatMessageRole.SYSTEM,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
