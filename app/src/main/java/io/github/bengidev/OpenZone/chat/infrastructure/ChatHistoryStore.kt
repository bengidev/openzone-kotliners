package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.domain.ChatMessage

/**
 * Persistence boundary for chat history.
 *
 * Mirrors the onboarding `OnboardingRepository` pattern: a pure interface in the
 * feature's infrastructure layer, with a concrete storage implementation
 * ([io.github.bengidev.openzone.chat.infrastructure.RoomChatHistoryStore]) kept
 * behind it so the [io.github.bengidev.openzone.chat.application.ChatComponent]
 * reducer stays free of Room/Android dependencies and remains unit-testable with
 * an in-memory double.
 *
 * Writes happen at **turn boundaries only** (see `ChatComponent`):
 *  - the user message is persisted on send,
 *  - the assistant message is persisted once on stream completion.
 *
 * Domain [ChatMessage] / [ChatConversation] types cross this boundary; the
 * mapping to and from the Room persistence representation happens inside the
 * concrete implementation, never leaking entities to callers.
 */
interface ChatHistoryStore {

    /** Inserts or updates the conversation row (idempotent on `id`). */
    suspend fun upsertConversation(conversation: ChatConversation)

    /**
     * Inserts or updates a single message for [conversationId] (idempotent on the
     * message `id`, so re-persisting a completed assistant turn is a no-op-ish
     * overwrite rather than a duplicate row).
     */
    suspend fun upsertMessage(conversationId: String, message: ChatMessage)

    /**
     * Loads all persisted messages for [conversationId] in chronological order.
     * Returns an empty list when the conversation has no stored history.
     */
    suspend fun loadMessages(conversationId: String): List<ChatMessage>
}
