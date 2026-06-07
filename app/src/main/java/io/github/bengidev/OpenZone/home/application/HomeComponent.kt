package io.github.bengidev.openzone.home.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.chat.application.ChatComponent
import io.github.bengidev.openzone.chat.application.ChatState
import io.github.bengidev.openzone.chat.infrastructure.ChatAPIClient
import io.github.bengidev.openzone.chat.infrastructure.ChatMockStreamingClient
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.home.domain.ComposerContextUsage
import io.github.bengidev.openzone.home.domain.ComposerModelOption
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.settings.application.SettingsComponent
import io.github.bengidev.openzone.shared.networking.MutableCredentialStore
import io.github.bengidev.openzone.shared.networking.ProviderPreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Decompose component for the home welcome + composer shell.
 * Mirrors iOS `MainChat` / `HomeFeature` — owns a child `ChatComponent`
 * whose state drives the welcome-vs-thread swap in the presenter.
 */
class HomeComponent(
    componentContext: ComponentContext,
    private val apiClient: ChatAPIClient = ChatMockStreamingClient.defaultClient(),
    private val credentialStore: MutableCredentialStore? = null,
    private val preferenceStore: ProviderPreferenceStore? = null,
    private val onSidebarToggle: () -> Unit = {}
) : ComponentContext by componentContext {

    private val _state = MutableValue(HomeState())
    val state: Value<HomeState> = _state

    // Chat is a child feature, owned by Home (mirrors iOS `HomeFeature` scoping `ChatFeature`).
    private val chatScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val chatComponent: ChatComponent = ChatComponent(
        apiClient = apiClient,
        scope = chatScope
    )

    /**
     * Settings is a child feature, owned by Home (same composition pattern as
     * [chatComponent]). Built only when both stores are wired from the app
     * entrypoint; in mock/preview wiring it stays null and the settings entry
     * is a no-op. Communication is via the shared store interfaces only — no
     * cross-feature presenter imports.
     */
    val settingsComponent: SettingsComponent? =
        if (credentialStore != null && preferenceStore != null) {
            SettingsComponent(
                componentContext = this,
                providers = ChatProviders.all,
                credentialStore = credentialStore,
                preferenceStore = preferenceStore,
                onClose = { _state.update { it.copy(isSettingsPresented = false) } }
            )
        } else {
            null
        }

    fun onSettingsTapped() {
        if (settingsComponent == null) return
        _state.update { it.copy(isSettingsPresented = true) }
    }

    fun onSettingsDismissed() {
        _state.update { it.copy(isSettingsPresented = false) }
    }

    fun onSidebarToggleTapped() {
        onSidebarToggle()
    }

    fun onDraftMessageChanged(text: String) {
        _state.update { it.copy(draftMessage = text) }
    }

    /**
     * Send the composer draft into the chat. The draft text is forwarded
     * to the `ChatComponent` and the local composer is cleared once the
     * chat owns the message. The `HomeState.draftMessage` is kept in sync
     * for the welcome-state composer view.
     */
    fun onSendTapped() {
        val current = _state.value
        if (!current.canSend) return
        val draft = current.draftMessage
        _state.update { it.copy(draftMessage = "") }
        chatComponent.onDraftChanged(draft)
        chatComponent.onSendTapped()
    }

    fun onStopTapped() {
        chatComponent.onStopTapped()
    }

    fun onClearThread() {
        chatComponent.onClearThread()
    }

    fun onModelSelected(model: ComposerModelOption) {
        _state.update { current ->
            val speedMode = if (model.availableSpeedModes.contains(current.speedMode)) {
                current.speedMode
            } else {
                ComposerSpeedMode.Standard
            }
            current.copy(selectedModel = model, speedMode = speedMode)
        }
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

    /** Exposed for `HomeScreen` to inspect chat-thread presence. */
    fun chatState(): ChatState = chatComponent.state.value
}
