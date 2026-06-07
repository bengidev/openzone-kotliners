package io.github.bengidev.openzone.settings.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.shared.networking.ChatProvider
import io.github.bengidev.openzone.shared.networking.MutableCredentialStore
import io.github.bengidev.openzone.shared.networking.ProviderPreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Decompose component for the Settings surface. Mirrors the established
 * `OnboardingComponent` pattern (MutableValue state + intent methods + a
 * Main-immediate scope for persistence).
 *
 * Talks only to the shared-seam interfaces [MutableCredentialStore] and
 * [ProviderPreferenceStore] — never to Chat/Home presenter code — so Settings
 * and Chat stay decoupled. The API-key secret never enters [SettingsState];
 * only a presence flag ([SettingsState.hasApiKey]) is surfaced.
 *
 * @param providers selectable providers; OpenRouter-first.
 * @param credentialStore encrypted key storage (write side).
 * @param preferenceStore persisted provider/model selection.
 * @param onClose invoked when the user dismisses the surface.
 */
class SettingsComponent(
    componentContext: ComponentContext,
    private val providers: List<ChatProvider>,
    private val credentialStore: MutableCredentialStore,
    private val preferenceStore: ProviderPreferenceStore,
    private val onClose: () -> Unit = {},
    mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
) : ComponentContext by componentContext {

    private val scope = mainScope

    private val _state = MutableValue(SettingsState(providers = providers))
    val state: Value<SettingsState> = _state

    init {
        loadPersistedState()
    }

    private fun loadPersistedState() {
        scope.launch {
            val persisted = preferenceStore.preference()
            val providerId = persisted?.providerId
                ?: providers.firstOrNull()?.id
            val models = providerId?.let { ModelCatalog.forProvider(it) } ?: emptyList()
            val modelId = persisted?.modelId
                ?: providerId?.let { ModelCatalog.defaultModelId(it) }
            _state.update {
                it.copy(
                    selectedProviderId = providerId,
                    models = models,
                    selectedModelId = modelId,
                    hasApiKey = providerId?.let(credentialStore::hasSecret) ?: false,
                    isLoaded = true
                )
            }
        }
    }

    /** Update the in-progress key text. The secret is not persisted until save. */
    fun onApiKeyDraftChanged(text: String) {
        _state.update { it.copy(apiKeyDraft = text) }
    }

    /** Persist the draft key (encrypted) for the selected provider, then clear the draft. */
    fun onSaveApiKey() {
        val current = _state.value
        val providerId = current.selectedProviderId ?: return
        if (current.apiKeyDraft.isBlank()) return
        credentialStore.setSecret(providerId, current.apiKeyDraft)
        _state.update { it.copy(apiKeyDraft = "", hasApiKey = true) }
    }

    /** Remove the stored key for the selected provider. */
    fun onClearApiKey() {
        val providerId = _state.value.selectedProviderId ?: return
        credentialStore.clear(providerId)
        _state.update { it.copy(apiKeyDraft = "", hasApiKey = false) }
    }

    /** Select a provider, refresh its curated model list, and persist the choice. */
    fun onProviderSelected(providerId: String) {
        if (providers.none { it.id == providerId }) return
        val models = ModelCatalog.forProvider(providerId)
        val modelId = _state.value.selectedModelId
            ?.takeIf { id -> models.any { it.id == id } }
            ?: ModelCatalog.defaultModelId(providerId)
        _state.update {
            it.copy(
                selectedProviderId = providerId,
                models = models,
                selectedModelId = modelId,
                hasApiKey = credentialStore.hasSecret(providerId)
            )
        }
        scope.launch {
            preferenceStore.setProvider(providerId)
            modelId?.let { preferenceStore.setModel(providerId, it) }
        }
    }

    /** Select a model within the current provider and persist it. */
    fun onModelSelected(modelId: String) {
        val providerId = _state.value.selectedProviderId ?: return
        if (_state.value.models.none { it.id == modelId }) return
        _state.update { it.copy(selectedModelId = modelId) }
        scope.launch { preferenceStore.setModel(providerId, modelId) }
    }

    fun onCloseTapped() = onClose()

    fun onDestroy() {
        scope.cancel()
    }
}
