package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import kotlinx.coroutines.flow.Flow

/**
 * Streaming chat client contract. The reducer consumes the flow and merges
 * deltas into the active turn by stable message ID.
 *
 * Mirrors iOS `ChatAPIClientProtocol` / `ChatAPIClient`.
 */
interface ChatAPIClient {
    fun stream(request: ChatRequest): Flow<ChatStreamingEvent>
}
