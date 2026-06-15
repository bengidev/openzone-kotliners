package io.github.bengidev.openzone.sidepanel.application

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.infrastructure.InMemoryChatHistoryStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SidePanelSessionComponentTest {

    @Test
    fun `renaming preserves pin state optimistically`() = runTest {
        val store = InMemoryChatHistoryStore()
        val unpinned = ChatConversation(id = "u1", title = "Unpinned")
        val pinned = ChatConversation(id = "p1", title = "Pinned", isPinned = true)
        store.conversations[unpinned.id] = unpinned
        store.conversations[pinned.id] = pinned

        val ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        val panel =
            SidePanelSessionComponent(
                componentContext = DefaultComponentContext(lifecycle = LifecycleRegistry()),
                historyStore = store,
                mainScope = backgroundScope,
                ioDispatcher = ioDispatcher
            )
        runCurrent()
        advanceUntilIdle()

        panel.onRenameConversation("u1", "Renamed unpinned")
        panel.onRenameConversation("p1", "Renamed pinned")

        val conversations = panel.state.value.conversations
        assertFalse(conversations.first { it.id == "u1" }.isPinned)
        assertTrue(conversations.first { it.id == "p1" }.isPinned)
    }

    @Test
    fun `pin derives new value from state not stale payload`() = runTest {
        val store = InMemoryChatHistoryStore()
        val target = ChatConversation(id = "c1", title = "Chat", isPinned = false)
        store.conversations[target.id] = target

        val ioDispatcher = UnconfinedTestDispatcher(testScheduler)
        val panel =
            SidePanelSessionComponent(
                componentContext = DefaultComponentContext(lifecycle = LifecycleRegistry()),
                historyStore = store,
                mainScope = backgroundScope,
                ioDispatcher = ioDispatcher
            )
        runCurrent()
        advanceUntilIdle()

        panel.onPinConversation(target.copy(isPinned = true))

        assertTrue(panel.state.value.conversations.single().isPinned)
    }
}
