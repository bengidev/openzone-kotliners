package io.github.bengidev.openzone.settings.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.settings.infrastructure.ModelCatalogFetcher
import io.github.bengidev.openzone.shared.networking.ChatModel
import io.github.bengidev.openzone.shared.networking.ChatProvider
import io.github.bengidev.openzone.shared.networking.ModelCatalogStore
import io.github.bengidev.openzone.shared.networking.MutableCredentialStore
import io.github.bengidev.openzone.shared.networking.ProviderPreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Decompose component for the Settings surface. Mirrors the established
 * `OnboardingComponent` pattern (MutableValue state + intent methods + a
 * Main-immediate scope for persistence).
 *
 * Talks only to the shared-seam interfaces [MutableCredentialStore],
 * [ProviderPreferenceStore], [ModelCatalogStore] and [ModelCatalogFetcher] —
 * never to Chat/Home presenter code — so Settings and Chat stay decoupled. The
 * API-key secret never enters [SettingsState]; only a presence flag
 * ([SettingsState.hasApiKey]) is surfaced.
 *
 * The model list shown is a hybrid catalog: a cached live list (when present
 * and fresh) layered over the curated free-model fallback. On open, if a
 * credential exists and the cached catalog is stale (or absent), a live fetch
 * is kicked off and the result cached with a timestamp.
 *
 * @param providers selectable providers; OpenRouter-first.
 * @param credentialStore encrypted key storage (write side).
 * @param preferenceStore persisted provider/model selection.
 * @param catalogStore timestamped cache of the live model catalog.
 * @param catalogFetcher live `/models` fetcher; may be `null` to disable live refresh.
 * @param onClose invoked when the user dismisses the surface.
 */
class SettingsComponent(
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
            val models = providerId?.let { resolveModels(it) } ?: emptyList()
            val modelId = persisted?.modelId
                ?: providerId?.let { ModelCatalog.defaultModelId(it) }
            _state.update {
                it.copy(
                    selectedProviderId = providerId,
                    models = models,
                    selectedModelId = modelId,
                    reasoningLevel = persisted?.reasoningLevel ?: ComposerReasoningLevel.Off,
                    hasApiKey = providerId?.let(credentialStore::hasSecret) ?: false,
                    isLoaded = true
                )
            }
            // Refresh the live catalog if a key is present and the cache is stale.
            providerId?.let { refreshCatalogIfStale(it) }
        }
    }

    /**
     * Resolves the model list for [providerId] from the cached live catalog
     * when present, otherwise the curated fallback. Synchronous read of the
     * cache only — never a network call.
     */
    private suspend fun resolveModels(providerId: String): List<ChatModel> {
        val cached = catalogStore?.cachedCatalog(providerId)?.models
        return cached?.takeIf { it.isNotEmpty() } ?: ModelCatalog.forProvider(providerId)
    }

    /**
     * Fetches the live catalog when a credential exists and the cache is
     * stale/absent, then caches it and updates state. Falls back silently to
     * the curated list on any failure. No-op when no fetcher/store is wired.
     */
    private suspend fun refreshCatalogIfStale(providerId: String) {
        val store = catalogStore ?: return
        val fetcher = catalogFetcher ?: return
        if (!credentialStore.hasSecret(providerId)) return

        val cached = store.cachedCatalog(providerId)
        val fresh = cached != null && !cached.isStale(now(), catalogTtlMs)
        if (fresh) return

        _state.update { it.copy(isLoadingModels = true) }
        val fetched = withContext(ioDispatcher) { fetcher.fetchSync(providerId) }
        if (fetched.isNullOrEmpty()) {
            _state.update { it.copy(isLoadingModels = false) }
            return
        }
        store.saveCatalog(providerId, fetched, now())
        _state.update { current ->
            if (current.selectedProviderId != providerId) {
                current.copy(isLoadingModels = false)
            } else {
                // Keep the selection only if it still exists in the live catalog;
                // a since-removed id resolves to null so the send gate stays closed
                // until the user re-picks a known model.
                val stillValid =
                    current.selectedModelId?.let { id -> fetched.any { it.id == id } } ?: false
                current.copy(
                    models = fetched,
                    selectedModelId = if (stillValid) current.selectedModelId else null,
                    isLoadingModels = false
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
        // A key just became available — pull the live catalog now.
        scope.launch { refreshCatalogIfStale(providerId) }
    }

    /** Remove the stored key for the selected provider. */
    fun onClearApiKey() {
        val providerId = _state.value.selectedProviderId ?: return
        credentialStore.clear(providerId)
        _state.update { it.copy(apiKeyDraft = "", hasApiKey = false) }
    }

    /** Select a provider, refresh its model list, and persist the choice. */
    fun onProviderSelected(providerId: String) {
        if (providers.none { it.id == providerId }) return
        scope.launch {
            val models = resolveModels(providerId)
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
            preferenceStore.setProvider(providerId)
            modelId?.let { preferenceStore.setModel(providerId, it) }
            refreshCatalogIfStale(providerId)
        }
    }

    /** Select a model within the current provider and persist it. */
    fun onModelSelected(modelId: String) {
        val providerId = _state.value.selectedProviderId ?: return
        if (_state.value.models.none { it.id == modelId }) return
        _state.update { it.copy(selectedModelId = modelId) }
        scope.launch { preferenceStore.setModel(providerId, modelId) }
    }

    /** Update the reasoning effort level and persist it. */
    fun onReasoningLevelSelected(level: ComposerReasoningLevel) {
        _state.update { it.copy(reasoningLevel = level) }
        scope.launch { preferenceStore.setReasoningLevel(level) }
    }

    fun onCloseTapped() = onClose()

    fun onDestroy() {
        scope.cancel()
    }

    private companion object {
        /** Catalog considered stale after 6 hours; refetched on next Settings open. */
        const val DEFAULT_CATALOG_TTL_MS = 6L * 60L * 60L * 1000L
    }
}
