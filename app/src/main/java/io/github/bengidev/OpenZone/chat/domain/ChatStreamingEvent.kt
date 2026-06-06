package io.github.bengidev.openzone.chat.domain

/**
 * Streaming event emitted by a `ChatAPIClient`. The reducer merges
 * deltas into the active turn by stable message ID.
 * Mirrors iOS `ChatStreamingEvent`.
 */
sealed interface ChatStreamingEvent {
    data class TextDelta(val delta: String) : ChatStreamingEvent
    data class ThinkingDelta(val delta: String) : ChatStreamingEvent
    data object Done : ChatStreamingEvent
    data class Error(val error: ChatStreamError) : ChatStreamingEvent
}
