package io.github.bengidev.openzone.sidepanel.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider
import io.github.bengidev.openzone.shared.externals.networking.ModelCatalogFetcher
import io.github.bengidev.openzone.shared.externals.networking.ModelCatalogStore
import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreferenceStore
import io.github.bengidev.openzone.shared.externals.security.MutableCredentialStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Decompose component for the side panel's setting scope — app preferences
 * surface (API key entry, provider/model selection, reasoning level).
 * Mirrors iOS `SidePanelSettingFeature`.
 */
class SidePanelSettingComponent(
    componentContext: ComponentContext,
    private val providers: List<ChatProvider>,
    private val credentialStore: MutableCredentialStore,
    private val preferenceStore: ProviderPreferenceStore,
    private val catalogStore: ModelCatalogStore? = null,
    private val catalogFetcher: ModelCatalogFetcher? = null,
    private val onClose: () -> Unit = {},
    private val catalogTtlMs: Long = DEFAULT_CATALOG_TTL_MS,
    private val now: () -> Long = System::currentTimeMillis,
    private val ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO,
    mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
) : ComponentContext by componentContext {

    data class State(
        val providers: List<ChatProvider> = emptyList(),
        val selectedProviderId: String? = null,
        val models: List<ChatModel> = emptyList(),
        val selectedModelId: String? = null,
        val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.Off,
        val apiKeyDraft: String = "",
        val hasApiKey: Boolean = false,
        val isLoaded: Boolean = false,
        val isLoadingModels: Boolean = false,
        val modelSupportsReasoning: Boolean = false
    ) {
        val selectedProvider: ChatProvider?
            get() = providers.firstOrNull { it.id == selectedProviderId }

        val selectedModel: ChatModel?
            get() = models.firstOrNull { it.id == selectedModelId }

        val canSaveKey: Boolean
            get() = apiKeyDraft.isNotBlank()
    }

    private val _state = MutableValue(State())
    val state: Value<State> = _state

    private val scope = mainScope

    init {
        scope.launch {
            val pref = preferenceStore.preference() ?: run {
                _state.update { it.copy(isLoaded = true, providers = providers) }
                return@launch
            }
            val providerId = pref.providerId
            val hasKey = credentialStore.hasSecret(providerId)
            val models = resolveAvailableModels(providerId)
            _state.update {
                it.copy(
                    providers = providers,
                    selectedProviderId = providerId,
                    selectedModelId = pref.modelId,
                    models = models,
                    reasoningLevel = pref.reasoningLevel,
                    hasApiKey = hasKey,
                    modelSupportsReasoning = models.firstOrNull { m -> m.id == pref.modelId }?.supportsReasoning == true,
                    isLoaded = true
                )
            }
            if (hasKey) refreshCatalogIfStale(providerId)
        }
    }

    // ---- Intents -----------------------------------------------------------

    fun onApiKeyDraftChanged(draft: String) {
        _state.update { it.copy(apiKeyDraft = draft) }
    }

    fun onSaveApiKey() {
        val draft = _state.value.apiKeyDraft.trim()
        if (draft.isEmpty()) return
        val providerId = _state.value.selectedProviderId ?: return
        scope.launch {
            credentialStore.setSecret(providerId, draft)
            _state.update { it.copy(apiKeyDraft = "", hasApiKey = true) }
        }
    }

    fun onClearApiKey() {
        val providerId = _state.value.selectedProviderId ?: return
        scope.launch {
            credentialStore.clear(providerId)
            _state.update { it.copy(hasApiKey = false) }
        }
    }

    fun onProviderSelected(providerId: String) {
        scope.launch {
            preferenceStore.setProvider(providerId)
            val models = resolveAvailableModels(providerId)
            _state.update {
                it.copy(
                    selectedProviderId = providerId,
                    selectedModelId = null,
                    models = models,
                    modelSupportsReasoning = false,
                    hasApiKey = credentialStore.hasSecret(providerId)
                )
            }
        }
    }

    fun onModelSelected(modelId: String) {
        val providerId = _state.value.selectedProviderId ?: return
        scope.launch {
            preferenceStore.setModel(providerId, modelId)
            val models = _state.value.models
            _state.update {
                it.copy(
                    selectedModelId = modelId,
                    modelSupportsReasoning = models.firstOrNull { m -> m.id == modelId }?.supportsReasoning == true
                )
            }
        }
    }

    fun onReasoningLevelSelected(level: ComposerReasoningLevel) {
        _state.update { it.copy(reasoningLevel = level) }
        scope.launch { preferenceStore.setReasoningLevel(level) }
    }

    fun onCloseTapped() {
        onClose()
    }

    // ---- Internal ----------------------------------------------------------

    private suspend fun resolveAvailableModels(providerId: String): List<ChatModel> {
        val cached = catalogStore?.cachedCatalog(providerId)?.models
        return cached?.takeIf { it.isNotEmpty() } ?: ModelCatalog.forProvider(providerId)
    }

    private fun refreshCatalogIfStale(providerId: String) {
        val store = catalogStore ?: return
        val fetcher = catalogFetcher ?: return
        val bgScope = CoroutineScope(ioDispatcher + SupervisorJob())
        bgScope.launch {
            val cached = store.cachedCatalog(providerId)
            val nowMs = now()
            if (cached != null && !cached.isStale(nowMs, catalogTtlMs)) return@launch

            _state.update { it.copy(isLoadingModels = true) }
            try {
                val models = fetcher.fetchSync(providerId) ?: ModelCatalog.forProvider(providerId)
                store.saveCatalog(providerId, models, nowMs)
                withContext(Dispatchers.Main) {
                    _state.update {
                        val currentModelId = it.selectedModelId
                        it.copy(
                            models = models,
                            modelSupportsReasoning = models.firstOrNull { m -> m.id == currentModelId }?.supportsReasoning == true,
                            isLoadingModels = false
                        )
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(isLoadingModels = false) }
                }
            }
        }
    }

    companion object {
        const val DEFAULT_CATALOG_TTL_MS = 300_000L
    }
}
