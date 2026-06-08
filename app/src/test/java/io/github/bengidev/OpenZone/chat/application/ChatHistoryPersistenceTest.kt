package io.github.bengidev.openzone.chat.application

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatMessageRole
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamError
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.chat.infrastructure.InMemoryChatHistoryStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Turn-boundary persistence tests for [ChatComponent] + [InMemoryChatHistoryStore].
 *
 * Pins the issue #6 contract:
 *  - user message persisted on send,
 *  - assistant message persisted once on completion (never per-delta),
 *  - an errored/killed turn keeps the user message but not the assistant text,
 *  - restoreHistory() repopulates state from the store (restart simulation).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatHistoryPersistenceTest {

    private class CannedClient(private val events: List<ChatStreamingEvent>) : ChatAPIClient {
        override fun stream(request: ChatRequest): Flow<ChatStreamingEvent> = flowOf(*events.toTypedArray())
    }

    private fun component(
        events: List<ChatStreamingEvent>,
        scope: CoroutineScope,
        store: InMemoryChatHistoryStore,
        initialState: ChatState = ChatState()
    ): ChatComponent = ChatComponent(
        apiClient = CannedClient(events),
        scope = scope,
        resolveProvider = { ChatProviders.openRouter },
        resolveModelId = { "deepseek/deepseek-r1:free" },
        canStartSend = { true },
        historyStore = store,
        initialState = initialState
    )

    @Test
    fun `user message is persisted on send`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        val component = component(
            events = listOf(ChatStreamingEvent.TextDelta("hi"), ChatStreamingEvent.Done),
            scope = scope,
            store = store
        )

        component.onDraftChanged("Hello")
        component.onSendTapped()

        val conversationId = component.state.value.conversation.id
        val persisted = store.loadMessages(conversationId)
        val user = persisted.filterIsInstance<ChatMessage.Text>()
            .single { it.message.role == ChatMessageRole.USER }
        assertEquals("Hello", user.message.content)
    }

    @Test
    fun `assistant message is persisted exactly once on completion`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        val component = component(
            events = listOf(
                ChatStreamingEvent.TextDelta("Hel"),
                ChatStreamingEvent.TextDelta("lo"),
                ChatStreamingEvent.TextDelta("!"),
                ChatStreamingEvent.Done
            ),
            scope = scope,
            store = store
        )

        component.onDraftChanged("Hi")
        component.onSendTapped()

        val conversationId = component.state.value.conversation.id
        val persisted = store.loadMessages(conversationId)
        val assistant = persisted.filterIsInstance<ChatMessage.Text>()
            .single { it.message.role == ChatMessageRole.ASSISTANT }
        // Full streamed content, marked complete, stored as a single row.
        assertEquals("Hello!", assistant.message.content)
        assertTrue(assistant.message.isComplete)
        // One user + one assistant upsert. No per-delta writes despite 3 deltas.
        assertEquals(2, store.messageUpsertCount)
    }

    @Test
    fun `errored turn keeps the user message but not assistant text`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        val component = component(
            events = listOf(
                ChatStreamingEvent.TextDelta("partial"),
                ChatStreamingEvent.Error(ChatStreamError("HTTP 500"))
            ),
            scope = scope,
            store = store
        )

        component.onDraftChanged("Question")
        component.onSendTapped()

        val conversationId = component.state.value.conversation.id
        val persisted = store.loadMessages(conversationId)
        // User message survived.
        assertTrue(
            persisted.filterIsInstance<ChatMessage.Text>()
                .any { it.message.role == ChatMessageRole.USER && it.message.content == "Question" }
        )
        // No assistant row persisted — only the user message was written.
        assertTrue(
            persisted.filterIsInstance<ChatMessage.Text>()
                .none { it.message.role == ChatMessageRole.ASSISTANT }
        )
        assertEquals(1, store.messageUpsertCount)
    }

    @Test
    fun `restoreHistory repopulates messages after a simulated restart`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()

        // First session: send a turn, which persists user + assistant messages.
        val first = component(
            events = listOf(ChatStreamingEvent.TextDelta("Answer"), ChatStreamingEvent.Done),
            scope = scope,
            store = store
        )
        first.onDraftChanged("Ask")
        first.onSendTapped()

        // Second session: fresh component over the SAME store (app restart).
        val second = component(
            events = emptyList(),
            scope = scope,
            store = store
        )
        assertTrue(second.state.value.messages.isEmpty())

        second.restoreHistory()

        val restored = second.state.value.messages
        assertTrue(restored.isNotEmpty())
        assertTrue(
            restored.filterIsInstance<ChatMessage.Text>()
                .any { it.message.role == ChatMessageRole.USER && it.message.content == "Ask" }
        )
        assertTrue(
            restored.filterIsInstance<ChatMessage.Text>()
                .any { it.message.role == ChatMessageRole.ASSISTANT && it.message.content == "Answer" }
        )
    }
}
