package io.github.bengidev.openzone.chat.infrastructure.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database hosting chat history (conversations + messages).
 *
 * Constructed in the app entrypoint (`MainActivity`) and injected into the chat
 * feature via constructor parameters — the reducer never touches Room directly.
 */
@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        const val DATABASE_NAME = "chat_history.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE conversations ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE conversations ADD COLUMN groupName TEXT")
            }
        }
    }
}
