package io.github.bengidev.openzone.home.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.chat.application.ChatComponent
import io.github.bengidev.openzone.chat.application.ChatState
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.chat.infrastructure.OpenAiCompatibleStreamingClient
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.settings.application.SettingsComponent
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.settings.infrastructure.DataStoreModelCatalogStore
import io.github.bengidev.openzone.settings.infrastructure.ModelCatalogFetcher
import io.github.bengidev.openzone.settings.infrastructure.OpenRouterModelFetcher
import io.github.bengidev.openzone.shared.networking.ChatModel
import io.github.bengidev.openzone.shared.networking.ChatProvider
import io.github.bengidev.openzone.shared.networking.CredentialStore
import io.github.bengidev.openzone.shared.networking.ModelCatalogStore
import io.github.bengidev.openzone.shared.networking.MutableCredentialStore
import io.github.bengidev.openzone.shared.networking.ProviderPreference
import io.github.bengidev.openzone.shared.networking.ProviderPreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Decompose component for the home welcome + composer shell.
 * Mirrors iOS `MainChat` / `HomeFeature` — owns a child `ChatComponent`
 * whose state drives the welcome-vs-thread swap in the presenter.
 *
 * Model identity is the dynamic [HomeState.selectedModelId] string sourced
 * from [ProviderPreferenceStore]; `ComposerModelOption` enum has been retired.
 * The composer model popup filters
 * [HomeState.availableModels] (live cache or curated fallback) via debounced
 * search and a free-tier toggle; selection writes back to the shared store so
 * Settings always reflects the same choice.
 *
 * `ComposerSpeedMode` is a purely cosmetic composer affordance and is excluded
 * from the provider request path.
 */
class HomeComponent(
    componentContext: ComponentContext,
    private val credentialStore: MutableCredentialStore? = null,
    private val preferenceStore: ProviderPreferenceStore? = null,
    private val catalogStore: ModelCatalogStore? = null,
    private val catalogFetcher: ModelCatalogFetcher? = null,
    apiClient: ChatAPIClient? = null,
    private val providers: List<ChatProvider> = ChatProviders.all,
    private val onSidebarToggle: () -> Unit = {}
) : ComponentContext by componentContext {

    private val _state = MutableValue(HomeState())
    val state: Value<HomeState> = _state

    private val chatScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val resolvedApiClient: ChatAPIClient =
        apiClient ?: credentialStore?.let { OpenAiCompatibleStreamingClient(credentialStore = it) }
            ?: OpenAiCompatibleStreamingClient(credentialStore = EmptyCredentialStore)

    @Volatile
    private var preference: ProviderPreference? = null

    val chatComponent: ChatComponent = ChatComponent(
        apiClient = resolvedApiClient,
        scope = chatScope,
        resolveProvider = { resolveProvider() },
        resolveModelId = { preference?.modelId },
        canStartSend = { isChatConfigured() }
    )

    private var debounceJob: Job? = null

    init {
        preferenceStore?.preferenceFlow
            ?.onEach { pref ->
                preference = pref
                val providerId = pref?.providerId
                val modelId = pref?.modelId
                val models = providerId?.let { resolveAvailableModels(it) } ?: emptyList()
                _state.update {
                    it.copy(
                        availableModels = models,
                        selectedModelId = modelId,
                        isChatConfigured = isChatConfigured()
                    )
                }
            }
            ?.launchIn(chatScope)
    }

    // ---- Helpers ----------------------------------------------------------

    private fun resolveProvider(): ChatProvider {
        val id = preference?.providerId
        return providers.firstOrNull { it.id == id }
            ?: providers.firstOrNull()
            ?: ChatProviders.openRouter
    }

    private fun isChatConfigured(): Boolean {
        val pref = preference ?: return false
        if (pref.modelId.isNullOrBlank()) return false
        return credentialStore?.hasSecret(pref.providerId) == true
    }

    private suspend fun resolveAvailableModels(providerId: String): List<ChatModel> {
        val cached = catalogStore?.cachedCatalog(providerId)?.models
        return cached?.takeIf { it.isNotEmpty() } ?: ModelCatalog.forProvider(providerId)
    }

    // ---- Settings child ---------------------------------------------------

    val settingsComponent: SettingsComponent? =
        if (credentialStore != null && preferenceStore != null) {
            SettingsComponent(
                componentContext = this,
                providers = ChatProviders.all,
                credentialStore = credentialStore,
                preferenceStore = preferenceStore,
                catalogStore = catalogStore,
                catalogFetcher = catalogFetcher,
                onClose = { _state.update { it.copy(isSettingsPresented = false) } }
            )
        } else null

    fun onSettingsTapped() {
        if (settingsComponent == null) return
        _state.update { it.copy(isSettingsPresented = true) }
    }

    fun onSettingsDismissed() {
        _state.update { it.copy(isSettingsPresented = false) }
    }

    // ---- Composer intents -------------------------------------------------

    fun onSidebarToggleTapped() = onSidebarToggle()

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

    // ---- Model popup intents ----------------------------------------------

    fun onModelPopupOpen() {
        _state.update { it.copy(isModelPopupPresented = true, modelSearchQuery = "", debouncedModelQuery = "") }
    }

    fun onModelPopupDismiss() {
        _state.update { it.copy(isModelPopupPresented = false) }
    }

    fun onModelSearchQueryChanged(query: String) {
        _state.update { it.copy(modelSearchQuery = query) }
        debounceJob?.cancel()
        debounceJob = chatScope.launch {
            delay(DEBOUNCE_MS)
            _state.update { it.copy(debouncedModelQuery = query) }
        }
    }

    fun onModelFilterFreeOnlyToggled() {
        _state.update { it.copy(modelFilterFreeOnly = !it.modelFilterFreeOnly) }
    }

    /**
     * Selects a model by [modelId] and persists it via the shared preference
     * store so Settings reflects the same choice immediately.
     */
    fun onModelSelected(modelId: String) {
        val providerId = preference?.providerId ?: providers.firstOrNull()?.id ?: return
        _state.update { it.copy(selectedModelId = modelId, isModelPopupPresented = false) }
        chatScope.launch { preferenceStore?.setModel(providerId, modelId) }
    }

    fun onReasoningLevelSelected(level: ComposerReasoningLevel) {
        _state.update { it.copy(reasoningLevel = level) }
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
