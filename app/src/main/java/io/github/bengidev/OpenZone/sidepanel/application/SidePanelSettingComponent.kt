package io.github.bengidev.openzone.sidepanel.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider
import io.github.bengidev.openzone.shared.externals.preference.ExternalAIProviderReasoningModel
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreferenceStore
import io.github.bengidev.openzone.shared.externals.security.MutableCredentialStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Decompose component for the side panel's setting scope — app preferences surface (API key entry,
 * provider selection, reasoning level). Mirrors iOS `SidePanelSettingFeature`.
 */
class SidePanelSettingComponent(
        componentContext: ComponentContext,
        private val providers: List<ChatProvider> = ChatProviders.all,
        private val credentialStore: MutableCredentialStore,
        private val preferenceStore: ProviderPreferenceStore,
        private val onClose: () -> Unit = {},
        private val onCredentialsChanged: () -> Unit = {},
        private val onReasoningModelChanged: () -> Unit = {},
        private val onProviderChanged: (String) -> Unit = {},
        modelSupportsReasoning: Boolean = false,
        selectedProviderId: String? = null,
        mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
) : ComponentContext by componentContext {

 data class State(
         val providers: List<ChatProvider> = emptyList(),
         val selectedProviderId: String = ChatProviders.openRouter.id,
         val draftApiKey: String = "",
         val hasStoredKey: Boolean = false,
         val errorMessage: String? = null,
         val reasoningModel: ExternalAIProviderReasoningModel =
                 ExternalAIProviderReasoningModel.High,
         val modelSupportsReasoning: Boolean = false,
         val isLoaded: Boolean = false
 ) {
  val canSave: Boolean
   get() = draftApiKey.isNotBlank()

  val selectedProvider: ChatProvider?
   get() = providers.firstOrNull { it.id == selectedProviderId }
 }

 private val _state =
         MutableValue(
                 State(
                         providers = providers,
                         selectedProviderId = selectedProviderId ?: ChatProviders.openRouter.id,
                         modelSupportsReasoning = modelSupportsReasoning
                 )
         )
 val state: Value<State> = _state

 private val scope = mainScope

 init {
  scope.launch { refreshFromStore() }
 }

 fun updateMirrors(modelSupportsReasoning: Boolean, selectedProviderId: String) {
  _state.update {
   it.copy(modelSupportsReasoning = modelSupportsReasoning, selectedProviderId = selectedProviderId)
  }
 }

 fun onApiKeyDraftChanged(draft: String) {
  _state.update { it.copy(draftApiKey = draft, errorMessage = null) }
 }

 fun onSaveApiKey() {
  val draft = _state.value.draftApiKey.trim()
  if (draft.isEmpty()) return
  val providerId = _state.value.selectedProviderId
  scope.launch {
   runCatching { credentialStore.setSecret(providerId, draft) }
           .onSuccess {
            _state.update { it.copy(draftApiKey = "", hasStoredKey = true, errorMessage = null) }
            onCredentialsChanged()
           }
           .onFailure {
            _state.update { it.copy(errorMessage = "Could not save the key to the Keychain.") }
           }
  }
 }

 fun onClearApiKey() {
  val providerId = _state.value.selectedProviderId
  scope.launch {
   runCatching { credentialStore.clear(providerId) }
           .onSuccess {
            _state.update { it.copy(hasStoredKey = false, draftApiKey = "", errorMessage = null) }
            onCredentialsChanged()
           }
           .onFailure {
            _state.update { it.copy(errorMessage = "Could not remove the key from the Keychain.") }
           }
  }
 }

 fun onProviderSelected(providerId: String) {
  if (providers.none { it.id == providerId }) return
  scope.launch {
   preferenceStore.setProvider(providerId)
   _state.update {
    it.copy(
            selectedProviderId = providerId,
            hasStoredKey = credentialStore.hasSecret(providerId),
            draftApiKey = ""
    )
   }
   onProviderChanged(providerId)
  }
 }

 fun onReasoningModelSelected(level: ExternalAIProviderReasoningModel) {
  _state.update { it.copy(reasoningModel = level) }
  scope.launch {
   preferenceStore.setReasoningLevel(level)
   onReasoningModelChanged()
  }
 }

 fun onCloseTapped() {
  onClose()
 }

 fun onAppear() {
  scope.launch { refreshFromStore() }
 }

 private suspend fun refreshFromStore() {
  val pref = preferenceStore.preference()
  val providerId = pref?.providerId ?: providers.firstOrNull()?.id ?: ChatProviders.openRouter.id
  _state.update {
   it.copy(
           providers = providers,
           selectedProviderId = providerId,
           reasoningModel = pref?.reasoningLevel ?: ExternalAIProviderReasoningModel.High,
           hasStoredKey = credentialStore.hasSecret(providerId),
           isLoaded = true
   )
  }
 }
}
