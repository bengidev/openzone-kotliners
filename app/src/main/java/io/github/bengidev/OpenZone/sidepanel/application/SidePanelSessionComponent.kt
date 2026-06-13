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
 */
class SidePanelSessionComponent(
    componentContext: ComponentContext,
    private val historyStore: ChatHistoryStore,
    private val onOpenConversation: (ChatConversation) -> Unit = {},
    private val onRenameConversation: (ChatConversation) -> Unit = {},
    private val onDeleteConversation: (ChatConversation) -> Unit = {},
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

    private val scope = mainScope

    init {
        scope.launch {
            reloadConversations()
        }
    }

    // ---- Intents -----------------------------------------------------------

    fun onToggleSidebar() {
        val isVisible = !_state.value.isSidebarVisible
        _state.update { it.copy(isSidebarVisible = isVisible) }
        if (isVisible) {
            scope.launch { reloadConversations() }
        }
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
        onOpenConversation(conversation)
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
        scope.launch {
            withContext(Dispatchers.IO) {
                historyStore.renameConversation(conversation.id, newTitle)
            }
            reloadConversations()
            val renamed = conversation.copy(title = newTitle)
            _state.update { it.copy(activeConversationId = renamed.id) }
            onRenameConversation(renamed)
        }
    }

    fun onDeleteConversation(conversation: ChatConversation) {
        scope.launch {
            withContext(Dispatchers.IO) {
                historyStore.deleteConversation(conversation.id)
            }
            reloadConversations()
            _state.update { state ->
                if (state.activeConversationId == conversation.id) {
                    state.copy(activeConversationId = null, isSidebarVisible = false)
                } else state
            }
            onDeleteConversation(conversation)
        }
    }

    fun setActiveConversationId(id: String?) {
        _state.update { it.copy(activeConversationId = id) }
    }

    // ---- Internal ----------------------------------------------------------

    private suspend fun reloadConversations() {
        val list = withContext(Dispatchers.IO) {
            historyStore.listConversations()
        }
        _state.update { it.copy(conversations = list) }
    }
}
