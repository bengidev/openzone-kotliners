package io.github.bengidev.openzone.chat.infrastructure.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database hosting chat history (conversations + messages).
 *
 * Constructed in the app entrypoint (`MainActivity`) and injected into the chat
 * feature via constructor parameters — the reducer never touches Room directly.
 */
@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        const val DATABASE_NAME = "chat_history.db"
    }
}
