package io.github.bengidev.openzone.chat.domain

/**
 * Role of a chat message participant.
 * Mirrors iOS `ChatMessageRole`.
 */
enum class ChatMessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
