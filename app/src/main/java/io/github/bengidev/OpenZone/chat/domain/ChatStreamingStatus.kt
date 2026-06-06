package io.github.bengidev.openzone.chat.domain

/**
 * Lifecycle status of the chat streaming pipeline.
 * Mirrors iOS `ChatStreamingStatus`.
 */
enum class ChatStreamingStatus {
    IDLE,
    RUNNING,
    DONE,
    FAILED
}
