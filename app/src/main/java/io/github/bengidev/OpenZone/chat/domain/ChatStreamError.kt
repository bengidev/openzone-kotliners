package io.github.bengidev.openzone.chat.domain

/**
 * Error payload for a failed chat stream.
 * Mirrors iOS `ChatStreamError`.
 */
data class ChatStreamError(val message: String)
