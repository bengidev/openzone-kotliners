package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.infrastructure.persistence.ChatHistoryDao
import io.github.bengidev.openzone.chat.infrastructure.persistence.ChatMessageMapper
import io.github.bengidev.openzone.chat.infrastructure.persistence.ConversationEntity

/**
 * Room-backed [ChatHistoryStore]. Translates domain types to/from persistence
 * entities at the boundary via [ChatMessageMapper] and delegates storage to
 * [ChatHistoryDao]. Holds no Android `Context` — it takes the DAO directly so it
 * can be unit-tested against an in-memory Room database.
 */
class RoomChatHistoryStore(
    private val dao: ChatHistoryDao
) : ChatHistoryStore {

    override suspend fun upsertConversation(conversation: ChatConversation) {
        dao.upsertConversation(
            ConversationEntity(
                id = conversation.id,
                title = conversation.title,
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt
            )
        )
    }

    override suspend fun upsertMessage(conversationId: String, message: ChatMessage) {
        dao.upsertMessage(ChatMessageMapper.toEntity(conversationId, message))
    }

    override suspend fun loadMessages(conversationId: String): List<ChatMessage> =
        dao.messagesFor(conversationId).map(ChatMessageMapper::toDomain)

    override suspend fun listConversations(): List<ChatConversation> =
        dao.conversations().map { entity ->
            ChatConversation(
                id = entity.id,
                title = entity.title,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
}
