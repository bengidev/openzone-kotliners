package io.github.bengidev.openzone.chat.infrastructure.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data access for chat history. Turn-boundary writes use REPLACE-on-conflict so
 * re-persisting a message by its stable id overwrites in place rather than
 * creating a duplicate row.
 *
 * Mirrors iOS `ChatHistoryClient` operations.
 */
@Dao
interface ChatHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC, id ASC")
    suspend fun messagesFor(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun conversation(conversationId: String): ConversationEntity?

    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC, createdAt DESC")
    suspend fun conversations(): List<ConversationEntity>

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("UPDATE conversations SET title = :title, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun renameConversation(conversationId: String, title: String, updatedAt: Long)

    @Query("UPDATE conversations SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun setPinned(conversationId: String, isPinned: Boolean, updatedAt: Long)

    @Query("UPDATE conversations SET groupName = :groupName WHERE id = :conversationId")
    suspend fun setGroup(conversationId: String, groupName: String?)

    @Query("SELECT DISTINCT groupName FROM conversations WHERE groupName IS NOT NULL ORDER BY groupName ASC")
    suspend fun listGroups(): List<String>
}
