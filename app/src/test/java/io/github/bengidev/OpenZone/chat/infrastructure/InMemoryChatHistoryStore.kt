package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.domain.ChatMessage

/** In-memory [ChatHistoryStore] double for unit tests. */
class InMemoryChatHistoryStore : ChatHistoryStore {

 val conversations = mutableMapOf<String, ChatConversation>()
 private val messagesByConversation = linkedMapOf<String, LinkedHashMap<String, ChatMessage>>()

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

 override suspend fun listConversations(): List<ChatConversation> =
         conversations.values.sortedWith(
                 compareByDescending<ChatConversation> { it.isPinned }.thenByDescending {
                  it.updatedAt
                 }
         )

 override suspend fun deleteConversation(conversationId: String) {
  conversations.remove(conversationId)
  messagesByConversation.remove(conversationId)
 }

 override suspend fun renameConversation(conversationId: String, title: String) {
  conversations[conversationId]?.let { existing ->
   conversations[conversationId] =
           existing.copy(title = title, updatedAt = System.currentTimeMillis())
  }
 }

 override suspend fun setPinned(conversationId: String, isPinned: Boolean) {
  conversations[conversationId]?.let { existing ->
   conversations[conversationId] =
           existing.copy(isPinned = isPinned, updatedAt = System.currentTimeMillis())
  }
 }

 override suspend fun setGroup(conversationId: String, groupName: String?) {
  conversations[conversationId]?.let { existing ->
   conversations[conversationId] =
           existing.copy(groupName = groupName?.trim()?.takeIf { it.isNotEmpty() })
  }
 }

 override suspend fun listGroups(): List<String> =
         conversations.values.mapNotNull { it.groupName }.distinct().sorted()

 fun messageIds(conversationId: String): List<String> =
         messagesByConversation[conversationId]?.keys?.toList().orEmpty()
}
