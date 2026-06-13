package io.github.bengidev.openzone.home.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.chat.application.ChatComponent
import io.github.bengidev.openzone.chat.application.ChatState
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
import io.github.bengidev.openzone.chat.infrastructure.ChatHistoryStore
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.chat.infrastructure.OpenAiCompatibleStreamingClient
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.settings.application.SettingsComponent
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.sidepanel.application.SidePanelSessionComponent
import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider
import io.github.bengidev.openzone.shared.externals.networking.ModelCatalogFetcher
import io.github.bengidev.openzone.shared.externals.networking.ModelCatalogStore
import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreference
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreferenceStore
import io.github.bengidev.openzone.shared.externals.security.CredentialStore
import io.github.bengidev.openzone.shared.externals.security.MutableCredentialStore
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
 * Mirrors iOS `HomeFeature` — owns a child `ChatComponent`
 * whose state drives the welcome-vs-thread swap in the presenter.
 *
 * Also composes the side panel's [SidePanelSessionComponent] (session scope,
 * saved-conversation browsing). Settings remains a separate child for now
 * (migration target: [SidePanelSettingComponent]).
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
    private val historyStore: ChatHistoryStore? = null,
    private val providers: List<ChatProvider> = ChatProviders.all
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
        resolveReasoningLevel = { resolveReasoningLevel() },
        canStartSend = { isChatConfigured() },
        historyStore = historyStore
    ).also { it.restoreHistory() }

    /** Side panel session scope — saved-conversation browser. Mirrors iOS `SidePanelSessionFeature`. */
    val sidePanelSessionComponent: SidePanelSessionComponent? =
        if (historyStore != null) {
            SidePanelSessionComponent(
                componentContext = this,
                historyStore = historyStore,
                onOpenConversation = { conversation ->
                    chatComponent.openConversation(conversation)
                    _state.update { it.copy(isSidebarPresented = false) }
                },
                onDeleteConversation = { conversation ->
                    if (chatComponent.state.value.conversation.id == conversation.id) {
                        chatComponent.resetToNewConversation()
                    }
                    _state.update { it.copy(isSidebarPresented = false) }
                }
            )
        } else null

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
                        reasoningLevel = pref?.reasoningLevel ?: ComposerReasoningLevel.Off,
                        isChatConfigured = isChatConfigured(),
                        hasApiKey = hasApiKey(),
                        hasLoadedPreference = true
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

    private fun resolveReasoningLevel(): ComposerReasoningLevel =
        preference?.reasoningLevel ?: ComposerReasoningLevel.Off

    private fun isChatConfigured(): Boolean {
        val pref = preference ?: return false
        if (pref.modelId.isNullOrBlank()) return false
        return credentialStore?.hasSecret(pref.providerId) == true
    }

    /** Whether a credential exists for the selected provider (model-agnostic). */
    private fun hasApiKey(): Boolean {
        val providerId = preference?.providerId
            ?: providers.firstOrNull()?.id
            ?: return false
        return credentialStore?.hasSecret(providerId) == true
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

    // ---- Sidebar intents (delegated to sidePanelSessionComponent) ----------

    fun onSidebarToggleTapped() {
        _state.update { it.copy(isSidebarPresented = !it.isSidebarPresented) }
        sidePanelSessionComponent?.onToggleSidebar()
    }

    fun onSidebarDismissed() {
        _state.update { it.copy(isSidebarPresented = false) }
        sidePanelSessionComponent?.onDismissSidebar()
    }

    /**
     * Reopens the selected conversation in the chat thread and closes the drawer.
     * Delegates to [SidePanelSessionComponent] which drives the chat component
     * via the [onOpenConversation] callback.
     */
    fun onConversationSelected(conversation: ChatConversation) {
        sidePanelSessionComponent?.onConversationSelected(conversation)
    }

    // ---- Composer intents -------------------------------------------------

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

    fun onModelSelected(modelId: String) {
        val providerId = preference?.providerId ?: providers.firstOrNull()?.id ?: return
        _state.update { it.copy(selectedModelId = modelId, isModelPopupPresented = false) }
        chatScope.launch { preferenceStore?.setModel(providerId, modelId) }
    }

    fun onReasoningLevelSelected(level: ComposerReasoningLevel) {
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
