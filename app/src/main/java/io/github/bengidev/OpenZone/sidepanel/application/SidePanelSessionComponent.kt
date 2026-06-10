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

/**
 * Decompose component for the side panel's session scope — the saved-conversation
 * browser (formerly "history chat"). Mirrors iOS `SidePanelSessionFeature`.
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
        val isSidebarVisible: Boolean = false,
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

    private val _state = MutableValue(
        State(activeConversationId = activeConversationId)
    )
    val state: Value<State> = _state

    init {
        mainScope.launch {
            val list = historyStore.listConversations()
            _state.update { it.copy(conversations = list) }
        }
    }

    // ---- Intents -----------------------------------------------------------

    fun onToggleSidebar() {
        val isVisible = !_state.value.isSidebarVisible
        _state.update { it.copy(isSidebarVisible = isVisible) }
        if (isVisible) reloadConversations()
    }

    fun onDismissSidebar() {
        _state.update { it.copy(isSidebarVisible = false) }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(historySearchQuery = query) }
    }

    fun onConversationSelected(conversation: ChatConversation) {
        _state.update {
            it.copy(isSidebarVisible = false, activeConversationId = conversation.id)
        }
        onOpenConversationDelegate(conversation)
    }

    fun onPinConversation(conversation: ChatConversation) {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            historyStore.setPinned(conversation.id, !conversation.isPinned)
            reloadConversations()
        }
    }

    fun onRenameConversation(conversation: ChatConversation, newTitle: String) {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            historyStore.renameConversation(conversation.id, newTitle)
            reloadConversations()
            val renamed = conversation.copy(title = newTitle)
            _state.update { it.copy(activeConversationId = renamed.id) }
            onRenameConversationDelegate(renamed)
        }
    }

    fun onDeleteConversation(conversation: ChatConversation) {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            historyStore.deleteConversation(conversation.id)
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
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val list = historyStore.listConversations()
            _state.update { it.copy(conversations = list) }
        }
    }
}
