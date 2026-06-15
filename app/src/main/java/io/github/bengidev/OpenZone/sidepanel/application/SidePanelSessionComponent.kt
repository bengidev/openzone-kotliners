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
 * Decompose component for the side panel's session scope — the saved-conversation browser (formerly
 * "history chat"). Mirrors iOS `SidePanelSessionFeature`.
 */
class SidePanelSessionComponent(
        componentContext: ComponentContext,
        private val historyStore: ChatHistoryStore,
        private val onOpenConversation: (ChatConversation) -> Unit = {},
        private val onRenameConversation: (String, String) -> Unit = { _, _ -> },
        private val onDeleteConversation: (String) -> Unit = {},
        private val onSettingsTapped: () -> Unit = {},
        activeConversationId: String? = null,
        mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
) : ComponentContext by componentContext {

 data class State(
         val isSidebarVisible: Boolean = false,
         val conversations: List<ChatConversation> = emptyList(),
         val historySearchQuery: String = "",
         val activeConversationId: String? = null,
         val availableGroups: List<String> = emptyList(),
         val expandedGroups: Set<String> = emptySet()
 ) {
  val filteredConversations: List<ChatConversation>
   get() {
    val query = historySearchQuery.trim()
    val base =
            if (query.isEmpty()) {
             conversations
            } else {
             conversations.filter { it.title.contains(query, ignoreCase = true) }
            }
    return SidePanelSessionSection.deduplicatedPinnedFirst(base)
   }

  val sections: List<SidePanelSessionSection>
   get() =
           SidePanelSessionSection.grouped(
                   conversations = filteredConversations,
                   expandedGroups = expandedGroups,
                   forceExpandGroups = historySearchQuery.trim().isNotEmpty()
           )

  val hasSearchResults: Boolean
   get() = filteredConversations.isNotEmpty()
 }

 private val _state = MutableValue(State(activeConversationId = activeConversationId))
 val state: Value<State> = _state

 private val scope = mainScope

 init {
  scope.launch { reloadConversations() }
 }

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

 fun onSettingsButtonTapped() {
  onSettingsTapped()
 }

 fun onSearchQueryChanged(query: String) {
  _state.update { it.copy(historySearchQuery = query) }
 }

 fun onConversationSelected(conversation: ChatConversation) {
  _state.update { it.copy(isSidebarVisible = false, activeConversationId = conversation.id) }
  onOpenConversation(conversation)
 }

 fun onPinConversation(conversation: ChatConversation) {
  val matching =
          _state.value.conversations.indices.filter {
           _state.value.conversations[it].id == conversation.id
          }
  if (matching.isEmpty()) return
  val newValue = !_state.value.conversations[matching.first()].isPinned
  _state.update { state ->
   val updated =
           state.conversations.map { item ->
            if (item.id == conversation.id) item.copy(isPinned = newValue) else item
           }
   state.copy(conversations = SidePanelSessionSection.deduplicatedPinnedFirst(updated))
  }
  scope.launch { withContext(Dispatchers.IO) { historyStore.setPinned(conversation.id, newValue) } }
 }

 fun onRenameConversation(conversationId: String, newTitle: String) {
  val trimmed = newTitle.trim()
  if (trimmed.isEmpty()) return
  val matching =
          _state.value.conversations.indices.filter {
           _state.value.conversations[it].id == conversationId
          }
  if (matching.isEmpty()) return
  val now = System.currentTimeMillis()
  _state.update { state ->
   val updated =
           state.conversations
                   .map { item ->
                    if (item.id == conversationId) item.copy(title = trimmed, updatedAt = now)
                    else item
                   }
                   .let { SidePanelSessionSection.sortedPinnedFirst(it) }
   state.copy(conversations = SidePanelSessionSection.deduplicatedPinnedFirst(updated))
  }
  scope.launch {
   withContext(Dispatchers.IO) { historyStore.renameConversation(conversationId, trimmed) }
   onRenameConversation(conversationId, trimmed)
  }
 }

 fun onDeleteConversation(conversation: ChatConversation) {
  _state.update { state ->
   val updated = state.conversations.filterNot { it.id == conversation.id }
   val activeId =
           if (state.activeConversationId == conversation.id) null else state.activeConversationId
   state.copy(
           conversations = updated,
           activeConversationId = activeId,
           isSidebarVisible =
                   if (state.activeConversationId == conversation.id) false
                   else state.isSidebarVisible
   )
  }
  scope.launch {
   withContext(Dispatchers.IO) {
    historyStore.deleteConversation(conversation.id)
    val groups = historyStore.listGroups()
    _state.update { it.copy(availableGroups = groups) }
   }
   if (_state.value.activeConversationId == null) {
    onDeleteConversation(conversation.id)
   }
  }
 }

 fun onConversationGroupChanged(conversationId: String, groupName: String?) {
  val normalized = groupName?.trim()?.takeIf { it.isNotEmpty() }
  val matching =
          _state.value.conversations.indices.filter {
           _state.value.conversations[it].id == conversationId
          }
  if (matching.isEmpty()) return
  if (normalized != null) {
   _state.update { it.copy(expandedGroups = it.expandedGroups + normalized) }
  }
  _state.update { state ->
   val updated =
           state.conversations
                   .map { item ->
                    if (item.id == conversationId) item.copy(groupName = normalized) else item
                   }
                   .let { SidePanelSessionSection.sortedPinnedFirst(it) }
   state.copy(conversations = SidePanelSessionSection.deduplicatedPinnedFirst(updated))
  }
  scope.launch {
   withContext(Dispatchers.IO) {
    historyStore.setGroup(conversationId, normalized)
    val groups = historyStore.listGroups()
    _state.update { it.copy(availableGroups = groups) }
   }
  }
 }

 fun onGroupHeaderToggled(groupName: String) {
  _state.update { state ->
   val expanded = state.expandedGroups.toMutableSet()
   if (!expanded.add(groupName)) expanded.remove(groupName)
   state.copy(expandedGroups = expanded)
  }
 }

 fun setActiveConversationId(id: String?) {
  _state.update { it.copy(activeConversationId = id) }
 }

 fun mirrorSidebarVisible(visible: Boolean) {
  _state.update { it.copy(isSidebarVisible = visible) }
 }

 private suspend fun reloadConversations() {
  val list = withContext(Dispatchers.IO) { historyStore.listConversations() }
  val groups = withContext(Dispatchers.IO) { historyStore.listGroups() }
  _state.update {
   it.copy(
           conversations = SidePanelSessionSection.deduplicatedPinnedFirst(list),
           availableGroups = groups
   )
  }
 }
}
