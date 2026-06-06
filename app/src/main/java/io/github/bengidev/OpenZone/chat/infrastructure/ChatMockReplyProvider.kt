package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.chat.domain.ChatTextMessages
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs

/**
 * Generates a chain-of-thought "reasoning" snippet for the latest user text.
 * Mirrors iOS `ChatMockReplyProvider.thinkingSnippet(for:)`.
 */
internal object ChatMockReplyProvider {

    fun thinkingSnippet(userText: String): String {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) {
            return ChatTextMessages.THINKING_EMPTY
        }
        val preview = trimmed.take(64)
        return ChatTextMessages.THINKING_STEPS.joinToString(separator = "") { step ->
            step.replace("%s", preview)
        }
    }

    /** Late "summary" reasoning chunk emitted after the answer — exercises merge path. */
    fun thinkingTail(userText: String): String? {
        if (userText.trim().isEmpty()) return null
        return ChatTextMessages.THINKING_TAIL
    }

    fun reply(userText: String): String {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return ChatTextMessages.EMPTY_GREETING
        val templateIndex = abs(trimmed.hashCode()) % ChatTextMessages.REPLY_TEMPLATES.size
        return ChatTextMessages.REPLY_TEMPLATES[templateIndex](trimmed)
    }
}
