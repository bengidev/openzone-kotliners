package io.github.bengidev.openzone.chat.application

import io.github.bengidev.openzone.chat.domain.ChatMessage
import io.github.bengidev.openzone.chat.domain.ChatRequest
import io.github.bengidev.openzone.chat.domain.ChatStreamError
import io.github.bengidev.openzone.chat.domain.ChatStreamingEvent
import io.github.bengidev.openzone.chat.domain.ChatStreamingStatus
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ChatComponent] state transitions, driven through the public
 * `send()` / `state` API with a canned [ChatAPIClient] stub (no network, no
 * mock-client dependency). Covers the contract called out in issue #4:
 *
 *  - reasoning + text deltas merge into the active turn by stable message ID
 *  - completion (`Done`) marks the open turn complete and clears streaming
 *  - errors surface in streamErrorMessage + FAILED status (banner renders off state)
 *  - send is hard-blocked without a selected model / stored credential
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatComponentTest {

    /** Canned client that replays a fixed event list, ignoring the request. */
    private class CannedClient(
        private val events: List<ChatStreamingEvent>,
        private val retryEvents: List<ChatStreamingEvent> = emptyList()
    ) : ChatAPIClient {
        private var attempt = 0

        override fun stream(request: ChatRequest): Flow<ChatStreamingEvent> {
            val script = if (attempt == 0) events else retryEvents
            attempt++
            return flowOf(*script.toTypedArray())
        }
    }

    /** Never completes — keeps the turn in RUNNING for loading-indicator tests. */
    private class HangingClient : ChatAPIClient {
        override fun stream(request: ChatRequest): Flow<ChatStreamingEvent> = flow {
            kotlinx.coroutines.awaitCancellation()
        }
    }

    private fun hangingComponent(
        scope: CoroutineScope,
        modelId: String? = "deepseek/deepseek-r1:free"
    ): ChatComponent = ChatComponent(
        apiClient = HangingClient(),
        scope = scope,
        resolveProvider = { ChatProviders.openRouter },
        resolveModelId = { modelId },
        canStartSend = { true }
    )

    private fun component(
        events: List<ChatStreamingEvent>,
        scope: CoroutineScope,
        modelId: String? = "deepseek/deepseek-r1:free",
        canStartSend: Boolean = true,
        retryEvents: List<ChatStreamingEvent> = emptyList()
    ): ChatComponent = ChatComponent(
        apiClient = CannedClient(events, retryEvents),
        scope = scope,
        resolveProvider = { ChatProviders.openRouter },
        resolveModelId = { modelId },
        canStartSend = { canStartSend }
    )

    private fun List<ChatMessage>.assistantText(): String =
        filterIsInstance<ChatMessage.Text>()
            .lastOrNull { it.message.role.name == "ASSISTANT" }
            ?.message?.content.orEmpty()

    private fun List<ChatMessage>.thinkingRows(): List<ChatMessage.Thinking> =
        filterIsInstance<ChatMessage.Thinking>()

    private fun List<ChatMessage>.systemRows(): List<ChatMessage.System> =
        filterIsInstance<ChatMessage.System>()

    @Test
    fun `text deltas merge into a single assistant row`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(
                ChatStreamingEvent.TextDelta("Hel"),
                ChatStreamingEvent.TextDelta("lo"),
                ChatStreamingEvent.Done
            ),
            scope = scope
        )

        component.onDraftChanged("Hi")
        component.onSendTapped()

        val messages = component.state.value.messages
        assertEquals("Hello", messages.assistantText())
        // Exactly one assistant text row was created, not one per delta.
        assertEquals(
            1,
            messages.filterIsInstance<ChatMessage.Text>().count { it.message.role.name == "ASSISTANT" }
        )
    }

    @Test
    fun `reasoning deltas merge into one thinking row by stable id`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(
                ChatStreamingEvent.ThinkingDelta("Let me "),
                ChatStreamingEvent.TextDelta("answer"),
                // Late reasoning chunk after the answer must land in the SAME row.
                ChatStreamingEvent.ThinkingDelta("think."),
                ChatStreamingEvent.Done
            ),
            scope = scope
        )

        component.onDraftChanged("Question")
        component.onSendTapped()

        val thinking = component.state.value.messages.thinkingRows()
        assertEquals(1, thinking.size)
        assertEquals("Let me think.", thinking.single().message.content)
        assertEquals("answer", component.state.value.messages.assistantText())
    }

    @Test
    fun `done marks the open turn complete and clears streaming`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(
                ChatStreamingEvent.ThinkingDelta("r"),
                ChatStreamingEvent.TextDelta("a"),
                ChatStreamingEvent.Done
            ),
            scope = scope
        )

        component.onDraftChanged("Hi")
        component.onSendTapped()

        val state = component.state.value
        assertEquals(ChatStreamingStatus.DONE, state.status)
        assertFalse(state.isStreaming)
        assertTrue(
            state.messages.filterIsInstance<ChatMessage.Text>()
                .last { it.message.role.name == "ASSISTANT" }.message.isComplete
        )
        assertTrue(state.messages.thinkingRows().single().message.isComplete)
    }

    @Test
    fun `error event surfaces streamErrorMessage and FAILED status`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(
                ChatStreamingEvent.TextDelta("partial"),
                ChatStreamingEvent.Error(ChatStreamError("HTTP 401: Unauthorized — check your API key."))
            ),
            scope = scope
        )

        component.onDraftChanged("Hi")
        component.onSendTapped()

        val state = component.state.value
        assertEquals(ChatStreamingStatus.FAILED, state.status)
        assertTrue(state.streamErrorMessage.orEmpty().contains("401"))
        assertTrue(state.messages.systemRows().isEmpty())
        assertEquals(1, state.messages.filterIsInstance<ChatMessage.Text>().size)
    }

    @Test
    fun `connection failure before assistant row keeps user message and error state`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(ChatStreamingEvent.Error(ChatStreamError("Cannot connect to model."))),
            scope = scope
        )

        component.onDraftChanged("Hello")
        component.onSendTapped()

        val state = component.state.value
        assertEquals(ChatStreamingStatus.FAILED, state.status)
        assertEquals("Cannot connect to model.", state.streamErrorMessage)
        assertEquals(1, state.messages.size)
        assertEquals("Hello", state.messages.filterIsInstance<ChatMessage.Text>().single().message.content)
        assertFalse(state.showLoadingIndicator)
    }

    @Test
    fun `dismiss clears error state`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(ChatStreamingEvent.Error(ChatStreamError("boom"))),
            scope = scope
        )

        component.onDraftChanged("Hi")
        component.onSendTapped()
        component.onErrorDismissed()

        val state = component.state.value
        assertNull(state.streamErrorMessage)
        assertEquals(ChatStreamingStatus.IDLE, state.status)
    }

    @Test
    fun `retry reissues without appending a second user message`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(ChatStreamingEvent.Error(ChatStreamError("network down"))),
            retryEvents = listOf(ChatStreamingEvent.TextDelta("Hello back"), ChatStreamingEvent.Done),
            scope = scope
        )

        component.onDraftChanged("Hi")
        component.onSendTapped()
        component.onRetryTapped()

        val state = component.state.value
        val userCount =
            state.messages.count {
                it is ChatMessage.Text && it.message.role.name == "USER"
            }
        assertEquals(1, userCount)
        assertEquals("Hello back", state.messages.assistantText())
        assertEquals(ChatStreamingStatus.DONE, state.status)
        assertNull(state.streamErrorMessage)
    }

    @Test
    fun `send is blocked when no model is selected`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(ChatStreamingEvent.TextDelta("x"), ChatStreamingEvent.Done),
            scope = scope,
            modelId = null
        )

        component.onDraftChanged("Hi")
        component.onSendTapped()

        // No turn was appended; status stayed idle.
        assertTrue(component.state.value.messages.isEmpty())
        assertEquals(ChatStreamingStatus.IDLE, component.state.value.status)
    }

    @Test
    fun `whitespace-only reasoning deltas do not create thinking row`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events = listOf(
                ChatStreamingEvent.ThinkingDelta("   "),
                ChatStreamingEvent.TextDelta("Answer"),
                ChatStreamingEvent.Done
            ),
            scope = scope
        )

        component.onDraftChanged("Question")
        component.onSendTapped()

        assertTrue(component.state.value.messages.thinkingRows().isEmpty())
        assertEquals("Answer", component.state.value.messages.assistantText())
    }

    @Test
    fun `empty stream completion surfaces an error`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(events = emptyList(), scope = scope)

        component.onDraftChanged("Hello")
        component.onSendTapped()

        val state = component.state.value
        assertEquals(ChatStreamingStatus.FAILED, state.status)
        assertEquals("No response received from the model.", state.streamErrorMessage)
    }

    @Test
    fun `done without assistant content surfaces an error`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(events = listOf(ChatStreamingEvent.Done), scope = scope)

        component.onDraftChanged("Hello")
        component.onSendTapped()

        val state = component.state.value
        assertEquals(ChatStreamingStatus.FAILED, state.status)
        assertEquals("No response received from the model.", state.streamErrorMessage)
        assertEquals(1, state.messages.size)
        assertTrue(state.showChatErrorBanner)
    }

    @Test
    fun `reasoning_details stream completes assistant turn in component`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(
            events =
                listOf(
                    ChatStreamingEvent.ThinkingDelta("Planning"),
                    ChatStreamingEvent.TextDelta("Hello! How can I help you today?"),
                    ChatStreamingEvent.Done
                ),
            scope = scope
        )

        component.onDraftChanged("hello")
        component.onSendTapped()

        val state = component.state.value
        assertEquals(ChatStreamingStatus.DONE, state.status)
        assertNull(state.streamErrorMessage)
        assertEquals("Hello! How can I help you today?", state.messages.assistantText())
        assertEquals("Planning", state.messages.thinkingRows().single().message.content)
    }

    @Test
    fun `loading indicator shows before first assistant delta`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = hangingComponent(scope = scope)

        component.onDraftChanged("Question")
        component.onSendTapped()

        assertTrue(component.state.value.showLoadingIndicator)
    }
}
