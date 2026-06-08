package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.domain.ChatMessage

/**
 * In-memory [ChatHistoryStore] double for unit tests. Records upserts in
 * insertion order and overwrites by message id (mirroring the REPLACE-on-conflict
 * contract of the Room DAO), so tests can assert turn-boundary write behavior
 * without a device or Robolectric.
 */
class InMemoryChatHistoryStore : ChatHistoryStore {

    val conversations = mutableMapOf<String, ChatConversation>()
    private val messagesByConversation = linkedMapOf<String, LinkedHashMap<String, ChatMessage>>()

    /** Total upsert calls, used to assert "persisted once" semantics. */
    var messageUpsertCount = 0
        private set

    override suspend fun upsertConversation(conversation: ChatConversation) {
        conversations[conversation.id] = conversation
    }

    override suspend fun upsertMessage(conversationId: String, message: ChatMessage) {
        messageUpsertCount++
        val bucket = messagesByConversation.getOrPut(conversationId) { linkedMapOf() }
        bucket[message.id] = message
    }

    override suspend fun loadMessages(conversationId: String): List<ChatMessage> =
        messagesByConversation[conversationId]?.values?.toList().orEmpty()

    fun messageIds(conversationId: String): List<String> =
        messagesByConversation[conversationId]?.keys?.toList().orEmpty()
}
