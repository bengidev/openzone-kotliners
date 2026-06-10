package io.github.bengidev.openzone.chat.domain

/**
 * Chat thread metadata. Messages live separately in `ChatState` so the
 * reducer can stream partial text without round-tripping through persistence.
 * Mirrors iOS `ChatConversation`.
 */
data class ChatConversation(
    val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)
