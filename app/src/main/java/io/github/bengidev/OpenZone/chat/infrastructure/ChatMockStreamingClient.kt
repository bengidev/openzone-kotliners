package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Local, network-free mock client that streams reasoning + answer text
 * chunk by chunk. Mirrors iOS `ChatMockStreamingClient` and the iOS
 * `ChatAPIClientProtocol`.
 */
class ChatMockStreamingClient(
    private val delayMillis: Long = 35L,
    private val thinkingDelayMillis: Long = 45L
) : ChatAPIClient {

    override fun stream(request: ChatRequest): Flow<ChatStreamingEvent> = flow {
        val userText = ChatRequest.latestUserTextIn(request.messages)
        val thinking = ChatMockReplyProvider.thinkingSnippet(userText)
        val reply = ChatMockReplyProvider.reply(userText)

        // Reasoning token-by-token.
        for (delta in chunked(thinking)) {
            if (thinkingDelayMillis > 0) delay(thinkingDelayMillis)
            emit(ChatStreamingEvent.ThinkingDelta(delta))
        }

        // Answer token-by-token.
        for (delta in chunked(reply)) {
            if (delayMillis > 0) delay(delayMillis)
            emit(ChatStreamingEvent.TextDelta(delta))
        }

        // Late "summary" reasoning chunk after the answer — exercises merge path
        // (must land in the same reasoning row, not a new one).
        val tail = ChatMockReplyProvider.thinkingTail(userText)
        if (tail != null) {
            if (thinkingDelayMillis > 0) delay(thinkingDelayMillis)
            emit(ChatStreamingEvent.ThinkingDelta(tail))
        }

        emit(ChatStreamingEvent.Done)
    }

    /** Word-sized streaming chunks (keeps trailing spaces). */
    private fun chunked(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        for (ch in text) {
            current.append(ch)
            if (ch == ' ') {
                chunks += current.toString()
                current.clear()
            }
        }
        if (current.isNotEmpty()) chunks += current.toString()
        return chunks
    }

    companion object {
        /** Comfortable typing pace — used in app. */
        fun defaultClient(): ChatMockStreamingClient = ChatMockStreamingClient()

        /** Zero-delay client for tests / previews. */
        fun fastClient(): ChatMockStreamingClient = ChatMockStreamingClient(
            delayMillis = 0L,
            thinkingDelayMillis = 0L
        )
    }
}
