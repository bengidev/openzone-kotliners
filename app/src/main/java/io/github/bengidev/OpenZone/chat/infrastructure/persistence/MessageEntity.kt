package io.github.bengidev.openzone.chat.infrastructure.persistence

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room persistence representation of a single chat message row.
 *
 * The domain message union ([io.github.bengidev.openzone.chat.domain.ChatMessage])
 * is flattened into a tagged row: [kind] discriminates Text / Thinking / System
 * variants, [role] carries the participant, and the remaining columns hold the
 * common payload. Mapping to and from the domain type lives in [ChatMessageMapper].
 *
 * Rows are scoped to a conversation via a foreign key with cascade delete so
 * removing a conversation cleans up its messages.
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    /** Discriminator: one of [ChatMessageMapper.KIND_TEXT]/`KIND_THINKING`/`KIND_SYSTEM`. */
    val kind: String,
    /** Participant role name (USER / ASSISTANT / SYSTEM). */
    val role: String,
    val content: String,
    val isComplete: Boolean,
    val timestamp: Long
)
