package io.github.bengidev.openzone.home.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.chat.application.ChatComponent
import io.github.bengidev.openzone.chat.application.ChatState
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
import io.github.bengidev.openzone.chat.infrastructure.ChatHistoryStore
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.chat.infrastructure.OpenAiCompatibleStreamingClient
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider
import io.github.bengidev.openzone.shared.externals.networking.ModelCatalogFetcher
import io.github.bengidev.openzone.shared.externals.networking.ModelCatalogStore
import io.github.bengidev.openzone.shared.externals.preference.ExternalAIProviderReasoningModel
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreference
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreferenceStore
import io.github.bengidev.openzone.shared.externals.security.CredentialStore
import io.github.bengidev.openzone.shared.externals.security.MutableCredentialStore
import io.github.bengidev.openzone.sidepanel.application.SidePanelComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Decompose component for the home welcome + composer shell. Mirrors iOS `HomeFeature` — owns a
 * child `ChatComponent` and composes the side panel module (`SidePanelComponent`) for session
 * browsing and settings.
 */
class HomeComponent(
        componentContext: ComponentContext,
        private val credentialStore: MutableCredentialStore? = null,
        private val preferenceStore: ProviderPreferenceStore? = null,
        private val catalogStore: ModelCatalogStore? = null,
        private val catalogFetcher: ModelCatalogFetcher? = null,
        apiClient: ChatAPIClient? = null,
        private val historyStore: ChatHistoryStore? = null,
        private val providers: List<ChatProvider> = ChatProviders.all
) : ComponentContext by componentContext {

 private val _state = MutableValue(HomeState())
 val state: Value<HomeState> = _state

 private val chatScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

 private val resolvedApiClient: ChatAPIClient =
         apiClient
                 ?: credentialStore?.let { OpenAiCompatibleStreamingClient(credentialStore = it) }
                         ?: OpenAiCompatibleStreamingClient(credentialStore = EmptyCredentialStore)

 @Volatile private var preference: ProviderPreference? = null

 val chatComponent: ChatComponent =
         ChatComponent(
                         apiClient = resolvedApiClient,
                         scope = chatScope,
                         resolveProvider = { resolveProvider() },
                         resolveModelId = { preference?.modelId },
                         resolveReasoningLevel = { resolveReasoningLevel() },
                         canStartSend = { isChatConfigured() },
                         historyStore = historyStore
                 )
                 .also { it.restoreHistory() }

 val sidePanelComponent: SidePanelComponent? =
         if (historyStore != null && credentialStore != null && preferenceStore != null) {
          SidePanelComponent(
                  componentContext = this,
                  historyStore = historyStore,
                  credentialStore = credentialStore,
                  preferenceStore = preferenceStore,
                  providers = providers,
                  onDelegate = ::handleSidePanelDelegate
          )
         } else null

 private var debounceJob: Job? = null

 init {
  preferenceStore
          ?.preferenceFlow
          ?.onEach { pref ->
           preference = pref
           val providerId = pref?.providerId
           val modelId = pref?.modelId
           val models = providerId?.let { resolveAvailableModels(it) } ?: emptyList()
           val reasoning = pref?.reasoningLevel ?: ExternalAIProviderReasoningModel.Off
           val supportsReasoning =
                   models.firstOrNull { it.id == modelId }?.supportsReasoning == true
           sidePanelComponent?.updateMirrors(
                   modelSupportsReasoning = supportsReasoning,
                   selectedProviderId = providerId
                                   ?: providers.firstOrNull()?.id ?: ChatProviders.openRouter.id
           )
           _state.update {
            it.copy(
                    availableModels = models,
                    selectedModelId = modelId,
                    reasoningLevel = reasoning,
                    isChatConfigured = isChatConfigured(),
                    hasApiKey = hasApiKey(),
                    hasLoadedPreference = true
            )
           }
          }
          ?.launchIn(chatScope)
 }

 private fun handleSidePanelDelegate(delegate: SidePanelComponent.Delegate) {
  when (delegate) {
   is SidePanelComponent.Delegate.OpenConversation -> {
    chatComponent.openConversation(delegate.conversation)
   }
   is SidePanelComponent.Delegate.ActiveConversationRenamed -> {
    if (chatComponent.state.value.conversation.id == delegate.id) {
     chatComponent.renameActiveConversation(delegate.title)
    }
   }
   is SidePanelComponent.Delegate.ActiveConversationDeleted -> {
    if (chatComponent.state.value.conversation.id == delegate.id) {
     chatComponent.resetToNewConversation()
    }
   }
   SidePanelComponent.Delegate.CredentialsChanged -> refreshCredentialGate()
   SidePanelComponent.Delegate.ReasoningModelChanged -> {
    chatScope.launch {
     val level =
             preferenceStore?.preference()?.reasoningLevel ?: ExternalAIProviderReasoningModel.Off
     _state.update { it.copy(reasoningLevel = level) }
    }
   }
   is SidePanelComponent.Delegate.ProviderChanged -> {
    chatScope.launch {
     preferenceStore?.setProvider(delegate.providerId)
     val models = resolveAvailableModels(delegate.providerId)
     _state.update {
      it.copy(availableModels = models, selectedModelId = null, isChatConfigured = false)
     }
     sidePanelComponent?.updateMirrors(
             modelSupportsReasoning = false,
             selectedProviderId = delegate.providerId
     )
     refreshCredentialGate()
     refreshCatalog(delegate.providerId)
    }
   }
  }
 }

 private fun refreshCredentialGate() {
  _state.update { it.copy(hasApiKey = hasApiKey(), isChatConfigured = isChatConfigured()) }
 }

 private suspend fun refreshCatalog(providerId: String) {
  val store = catalogStore ?: return
  val fetcher = catalogFetcher ?: return
  if (credentialStore?.hasSecret(providerId) != true) return
  runCatching { fetcher.fetchSync(providerId) ?: ModelCatalog.forProvider(providerId) }.onSuccess {
          models ->
   store.saveCatalog(providerId, models, System.currentTimeMillis())
   _state.update { it.copy(availableModels = models) }
  }
 }

 private fun resolveProvider(): ChatProvider {
  val id = preference?.providerId
  return providers.firstOrNull { it.id == id }
          ?: providers.firstOrNull() ?: ChatProviders.openRouter
 }

 private fun resolveReasoningLevel(): ExternalAIProviderReasoningModel =
         preference?.reasoningLevel ?: ExternalAIProviderReasoningModel.Off

 private fun isChatConfigured(): Boolean {
  val pref = preference ?: return false
  if (pref.modelId.isNullOrBlank()) return false
  return credentialStore?.hasSecret(pref.providerId) == true
 }

 private fun hasApiKey(): Boolean {
  val providerId = preference?.providerId ?: providers.firstOrNull()?.id ?: return false
  return credentialStore?.hasSecret(providerId) == true
 }

 private suspend fun resolveAvailableModels(providerId: String): List<ChatModel> {
  val cached = catalogStore?.cachedCatalog(providerId)?.models
  return cached?.takeIf { it.isNotEmpty() } ?: ModelCatalog.forProvider(providerId)
 }

 fun onSettingsTapped() {
  sidePanelComponent?.presentSettings()
 }

 fun onSidebarToggleTapped() {
  val activeId =
          chatComponent.state.value.conversation.id.takeIf { chatComponent.state.value.hasMessages }
  sidePanelComponent?.onSidebarToggleTapped(activeId)
 }

 fun onSidebarDismissed() {
  sidePanelComponent?.onSidebarDismissed()
 }

 fun onNewConversationTapped() {
  chatComponent.resetToNewConversation()
 }

 fun onDraftMessageChanged(text: String) {
  _state.update { it.copy(draftMessage = text) }
 }

 fun onSendTapped() {
  val current = _state.value
  if (!current.canSend) return
  val draft = current.draftMessage
  _state.update { it.copy(draftMessage = "") }
  chatComponent.onDraftChanged(draft)
  chatComponent.onSendTapped()
 }

 fun onStopTapped() = chatComponent.onStopTapped()
 fun onClearThread() = chatComponent.onClearThread()

 fun onModelPopupOpen() {
  _state.update {
   it.copy(isModelPopupPresented = true, modelSearchQuery = "", debouncedModelQuery = "")
  }
 }

 fun onModelPopupDismiss() {
  _state.update { it.copy(isModelPopupPresented = false) }
 }

 fun onModelSearchQueryChanged(query: String) {
  _state.update { it.copy(modelSearchQuery = query) }
  debounceJob?.cancel()
  debounceJob =
          chatScope.launch {
           delay(DEBOUNCE_MS)
           _state.update { it.copy(debouncedModelQuery = query) }
          }
 }

 fun onModelFilterFreeOnlyToggled() {
  _state.update { it.copy(modelFilterFreeOnly = !it.modelFilterFreeOnly) }
 }

 fun onModelSelected(modelId: String) {
  val providerId = preference?.providerId ?: providers.firstOrNull()?.id ?: return
  _state.update { it.copy(selectedModelId = modelId, isModelPopupPresented = false) }
  val supportsReasoning =
          _state.value.availableModels.firstOrNull { it.id == modelId }?.supportsReasoning == true
  sidePanelComponent?.updateMirrors(
          modelSupportsReasoning = supportsReasoning,
          selectedProviderId = providerId
  )
  chatScope.launch { preferenceStore?.setModel(providerId, modelId) }
 }

 fun onReasoningLevelSelected(level: ExternalAIProviderReasoningModel) {
  _state.update { it.copy(reasoningLevel = level) }
  chatScope.launch { preferenceStore?.setReasoningLevel(level) }
 }

 fun onSpeedModeSelected(mode: ComposerSpeedMode) {
  _state.update { it.copy(speedMode = mode) }
 }

 fun onAttachmentTapped() = Unit
 fun onMicrophoneTapped() = Unit

 fun onContextUsageTapped() {
  _state.update { it.copy(isContextUsagePresented = !it.isContextUsagePresented) }
 }

 fun onContextUsageDismissed() {
  _state.update { it.copy(isContextUsagePresented = false) }
 }

 fun chatState(): ChatState = chatComponent.state.value

 private companion object {
  const val DEBOUNCE_MS = 300L
 }
}

private object EmptyCredentialStore : CredentialStore {
 override fun secretFor(providerId: String): String? = null
}
