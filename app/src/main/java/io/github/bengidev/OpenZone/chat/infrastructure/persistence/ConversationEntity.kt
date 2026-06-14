package io.github.bengidev.openzone.chat.infrastructure.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room persistence representation of a chat conversation.
 *
 * Kept separate from the domain [io.github.bengidev.openzone.chat.domain.ChatConversation]
 * so the storage schema can evolve independently; mapping happens at the store
 * boundary in [ChatMessageMapper].
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val groupName: String? = null
)
