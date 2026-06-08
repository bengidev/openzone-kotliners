package io.github.bengidev.openzone.chat.infrastructure.persistence

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatSystemMessage
import io.github.bengidev.openzone.chat.domain.ChatTextMessage
import io.github.bengidev.openzone.chat.domain.ChatThinkingMessage

/**
 * Maps between the domain [ChatMessage] union and the flat [MessageEntity] row.
 *
 * The mapping is total and round-trips: `toEntity(m).toDomain() == m` for every
 * variant. Unknown discriminators decode defensively to a System row rather than
 * throwing, so a forward-incompatible row can never crash history restore.
 */
object ChatMessageMapper {

    const val KIND_TEXT = "text"
    const val KIND_THINKING = "thinking"
    const val KIND_SYSTEM = "system"

    fun toEntity(conversationId: String, message: ChatMessage): MessageEntity = when (message) {
        is ChatMessage.Text -> MessageEntity(
            id = message.id,
            conversationId = conversationId,
            kind = KIND_TEXT,
            role = message.message.role.name,
            content = message.message.content,
            isComplete = message.message.isComplete,
            timestamp = message.timestamp
        )

        is ChatMessage.Thinking -> MessageEntity(
            id = message.id,
            conversationId = conversationId,
            kind = KIND_THINKING,
            role = message.message.role.name,
            content = message.message.content,
            isComplete = message.message.isComplete,
            timestamp = message.timestamp
        )

        is ChatMessage.System -> MessageEntity(
            id = message.id,
            conversationId = conversationId,
            kind = KIND_SYSTEM,
            role = message.message.role.name,
            content = message.message.content,
            isComplete = true,
            timestamp = message.timestamp
        )
    }

    fun toDomain(entity: MessageEntity): ChatMessage = when (entity.kind) {
        KIND_TEXT -> ChatMessage.Text(
            ChatTextMessage(
                id = entity.id,
                role = roleOf(entity.role),
                content = entity.content,
                isComplete = entity.isComplete,
                timestamp = entity.timestamp
            )
        )

        KIND_THINKING -> ChatMessage.Thinking(
            ChatThinkingMessage(
                id = entity.id,
                role = roleOf(entity.role),
                content = entity.content,
                isComplete = entity.isComplete,
                timestamp = entity.timestamp
            )
        )

        else -> ChatMessage.System(
            ChatSystemMessage(
                id = entity.id,
                role = ChatMessageRole.SYSTEM,
                content = entity.content,
                timestamp = entity.timestamp
            )
        )
    }

    private fun roleOf(raw: String): ChatMessageRole =
        runCatching { ChatMessageRole.valueOf(raw) }.getOrDefault(ChatMessageRole.ASSISTANT)
}
