package io.github.bengidev.openzone.sidepanel.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.infrastructure.ChatHistoryStore
import io.github.bengidev.openzone.sidepanel.domain.SidePanelSessionSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Decompose component for the side panel's session scope — the saved-conversation
 * browser (formerly "history chat"). Mirrors iOS `SidePanelSessionFeature`.
 *
 * Sidebar presentation (open/close) is owned by the parent [io.github.bengidev.openzone.home.application.HomeComponent];
 * this component owns the conversation list, search query, and active-conversation highlight.
 */
class SidePanelSessionComponent(
    componentContext: ComponentContext,
    private val historyStore: ChatHistoryStore,
    private val onOpenConversationDelegate: (ChatConversation) -> Unit = {},
    private val onRenameConversationDelegate: (ChatConversation) -> Unit = {},
    private val onDeleteConversationDelegate: (ChatConversation) -> Unit = {},
    private val onSettingsButtonTappedDelegate: () -> Unit = {},
    activeConversationId: String? = null,
    mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
) : ComponentContext by componentContext {

    data class State(
        val conversations: List<ChatConversation> = emptyList(),
        val historySearchQuery: String = "",
        val activeConversationId: String? = null
    ) {
        val filteredConversations: List<ChatConversation>
            get() {
                val query = historySearchQuery.trim().lowercase()
                if (query.isEmpty()) return conversations
                return conversations.filter { it.title.lowercase().contains(query) }
            }

        val sections: List<SidePanelSessionSection>
            get() = SidePanelSessionSection.grouped(filteredConversations)
    }

    private val scope = mainScope

    private val _state = MutableValue(
        State(activeConversationId = activeConversationId)
    )
    val state: Value<State> = _state

    // ---- Intents -----------------------------------------------------------

    /** Reloads the authoritative conversation list when the sidebar opens. */
    fun onSidebarOpened() {
        reloadConversations()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(historySearchQuery = query) }
    }

    fun onConversationSelected(conversation: ChatConversation) {
        _state.update { it.copy(activeConversationId = conversation.id) }
        onOpenConversationDelegate(conversation)
    }

    fun onPinConversation(conversation: ChatConversation) {
        scope.launch {
            withContext(Dispatchers.IO) {
                historyStore.setPinned(conversation.id, !conversation.isPinned)
            }
            reloadConversations()
        }
    }

    fun onRenameConversation(conversation: ChatConversation, newTitle: String) {
        val title = newTitle.trim()
        if (title.isEmpty()) return
        scope.launch {
            withContext(Dispatchers.IO) {
                historyStore.renameConversation(conversation.id, title)
            }
            reloadConversations()
            val renamed = conversation.copy(title = title)
            _state.update { it.copy(activeConversationId = renamed.id) }
            onRenameConversationDelegate(renamed)
        }
    }

    fun onDeleteConversation(conversation: ChatConversation) {
        scope.launch {
            withContext(Dispatchers.IO) {
                historyStore.deleteConversation(conversation.id)
            }
            reloadConversations()
            onDeleteConversationDelegate(conversation)
        }
    }

    fun onSettingsButtonTapped() {
        onSettingsButtonTappedDelegate()
    }

    fun setActiveConversationId(id: String?) {
        _state.update { it.copy(activeConversationId = id) }
    }

    // ---- Internal ----------------------------------------------------------

    private fun reloadConversations() {
        scope.launch {
            val list = withContext(Dispatchers.IO) { historyStore.listConversations() }
            _state.update { it.copy(conversations = list) }
        }
    }
}
