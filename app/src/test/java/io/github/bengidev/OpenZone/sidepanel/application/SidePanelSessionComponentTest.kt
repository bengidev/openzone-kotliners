package io.github.bengidev.openzone.sidepanel.application

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.infrastructure.InMemoryChatHistoryStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SidePanelSessionComponentTest {

    private fun component(
        store: InMemoryChatHistoryStore = InMemoryChatHistoryStore(),
        scope: CoroutineScope,
        onDelete: (ChatConversation) -> Unit = {}
    ): SidePanelSessionComponent {
        val lifecycle = LifecycleRegistry()
        return SidePanelSessionComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            historyStore = store,
            onDeleteConversationDelegate = onDelete,
            mainScope = scope
        )
    }

    @Test
    fun `onSidebarOpened loads conversations from store`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        store.upsertConversation(ChatConversation(id = "a", title = "Alpha", updatedAt = 2))
        store.upsertConversation(ChatConversation(id = "b", title = "Beta", updatedAt = 1))
        val component = component(store = store, scope = scope)

        component.onSidebarOpened()

        assertEquals(listOf("a", "b"), component.state.value.conversations.map { it.id })
    }

    @Test
    fun `search query filters conversations by title`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        store.upsertConversation(ChatConversation(id = "a", title = "Kotlin help"))
        store.upsertConversation(ChatConversation(id = "b", title = "Swift help"))
        val component = component(store = store, scope = scope)
        component.onSidebarOpened()

        component.onSearchQueryChanged("kotlin")

        assertEquals(listOf("a"), component.state.value.filteredConversations.map { it.id })
    }

    @Test
    fun `pin toggles persisted flag and reloads list`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        val conversation = ChatConversation(id = "a", title = "Alpha", updatedAt = 2)
        store.upsertConversation(conversation)
        val component = component(store = store, scope = scope)
        component.onSidebarOpened()

        component.onPinConversation(conversation)

        assertTrue(store.conversations["a"]?.isPinned == true)
        assertTrue(component.state.value.conversations.first().isPinned)
    }

    @Test
    fun `rename ignores blank titles`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        val conversation = ChatConversation(id = "a", title = "Alpha")
        store.upsertConversation(conversation)
        val component = component(store = store, scope = scope)

        component.onRenameConversation(conversation, "   ")

        assertEquals("Alpha", store.conversations["a"]?.title)
    }

    @Test
    fun `delete removes conversation and notifies delegate`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val store = InMemoryChatHistoryStore()
        val conversation = ChatConversation(id = "a", title = "Alpha")
        store.upsertConversation(conversation)
        var deleted: ChatConversation? = null
        val component = component(
            store = store,
            scope = scope,
            onDelete = { deleted = it }
        )
        component.onSidebarOpened()

        component.onDeleteConversation(conversation)

        assertNull(store.conversations["a"])
        assertEquals("a", deleted?.id)
        assertTrue(component.state.value.conversations.isEmpty())
    }

    @Test
    fun `setActiveConversationId updates highlight state`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(scope = scope)

        component.setActiveConversationId("active-id")

        assertEquals("active-id", component.state.value.activeConversationId)
    }
}
