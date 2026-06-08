package io.github.bengidev.openzone.chat.infrastructure.persistence

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatMessages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Round-trip tests for [ChatMessageMapper]: every domain variant must survive
 * `toEntity -> toDomain` unchanged, and unknown discriminators must decode
 * defensively rather than throw.
 */
class ChatMessageMapperTest {

    private val conversationId = "conv-1"

    @Test
    fun `text message round-trips`() {
        val original = ChatMessages.text(
            id = "user-1",
            role = ChatMessageRole.USER,
            content = "Hello there",
            isComplete = true,
            timestamp = 1000L
        )
        val restored = ChatMessageMapper.toDomain(
            ChatMessageMapper.toEntity(conversationId, original)
        )
        assertEquals(original, restored)
    }

    @Test
    fun `assistant text preserves completion flag and content`() {
        val original = ChatMessages.text(
            id = "assistant-1",
            role = ChatMessageRole.ASSISTANT,
            content = "Full streamed answer",
            isComplete = true,
            timestamp = 2000L
        )
        val entity = ChatMessageMapper.toEntity(conversationId, original)
        assertEquals(ChatMessageMapper.KIND_TEXT, entity.kind)
        assertEquals("ASSISTANT", entity.role)
        assertEquals(conversationId, entity.conversationId)
        assertEquals(original, ChatMessageMapper.toDomain(entity))
    }

    @Test
    fun `thinking message round-trips`() {
        val original = ChatMessages.thinking(
            id = "thinking-1",
            content = "Let me reason",
            isComplete = true,
            timestamp = 3000L
        )
        val restored = ChatMessageMapper.toDomain(
            ChatMessageMapper.toEntity(conversationId, original)
        )
        assertEquals(original, restored)
    }

    @Test
    fun `system message round-trips`() {
        val original = ChatMessages.system(
            id = "system-1",
            content = "Something went wrong",
            timestamp = 4000L
        )
        val restored = ChatMessageMapper.toDomain(
            ChatMessageMapper.toEntity(conversationId, original)
        )
        assertEquals(original, restored)
    }

    @Test
    fun `unknown kind decodes to a system row instead of throwing`() {
        val forwardIncompatible = MessageEntity(
            id = "x-1",
            conversationId = conversationId,
            kind = "future-kind",
            role = "ASSISTANT",
            content = "payload",
            isComplete = true,
            timestamp = 5000L
        )
        val restored = ChatMessageMapper.toDomain(forwardIncompatible)
        assertTrue(restored is ChatMessage.System)
        assertEquals("payload", (restored as ChatMessage.System).message.content)
    }

    @Test
    fun `unknown role decodes defensively without throwing`() {
        val entity = MessageEntity(
            id = "t-1",
            conversationId = conversationId,
            kind = ChatMessageMapper.KIND_TEXT,
            role = "ROBOT",
            content = "hi",
            isComplete = true,
            timestamp = 6000L
        )
        val restored = ChatMessageMapper.toDomain(entity)
        assertTrue(restored is ChatMessage.Text)
    }
}
