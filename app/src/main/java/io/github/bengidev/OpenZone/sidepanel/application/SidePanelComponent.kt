package io.github.bengidev.openzone.sidepanel.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.chat.infrastructure.ChatHistoryStore
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreferenceStore
import io.github.bengidev.openzone.shared.externals.security.MutableCredentialStore

/**
 * Host component for the side panel module. Composes the session browser and
 * settings sheet sub-scopes and surfaces delegate outputs to Home.
 * Mirrors iOS `SidePanelFeature`.
 */
class SidePanelComponent(
    componentContext: ComponentContext,
    historyStore: ChatHistoryStore,
    private val credentialStore: MutableCredentialStore,
    private val preferenceStore: ProviderPreferenceStore,
    providers: List<ChatProvider> = ChatProviders.all,
    private val onDelegate: (Delegate) -> Unit = {}
) : ComponentContext by componentContext {

    sealed interface Delegate {
        data class OpenConversation(val conversation: ChatConversation) : Delegate
        data class ActiveConversationRenamed(val id: String, val title: String) : Delegate
        data class ActiveConversationDeleted(val id: String) : Delegate
        data object CredentialsChanged : Delegate
        data object ReasoningModelChanged : Delegate
        data class ProviderChanged(val providerId: String) : Delegate
    }

    data class State(
        val isSettingPresented: Boolean = false,
        val modelSupportsReasoning: Boolean = false,
        val selectedProviderId: String = ChatProviders.openRouter.id
    )

    private val _state = MutableValue(State())
    val state: Value<State> = _state

    val sessionComponent: SidePanelSessionComponent

    private var settingComponent: SidePanelSettingComponent? = null

    init {
        sessionComponent = SidePanelSessionComponent(
            componentContext = this,
            historyStore = historyStore,
            onOpenConversation = { conversation ->
                onDelegate(Delegate.OpenConversation(conversation))
            },
            onRenameConversation = { id, title ->
                onDelegate(Delegate.ActiveConversationRenamed(id, title))
            },
            onDeleteConversation = { id ->
                onDelegate(Delegate.ActiveConversationDeleted(id))
            },
            onSettingsTapped = ::onSettingsButtonTapped
        )
    }

    val setting: SidePanelSettingComponent?
        get() = settingComponent

    val isSidebarVisible: Boolean
        get() = sessionComponent.state.value.isSidebarVisible

    fun onSidebarToggleTapped(activeConversationId: String?) {
        sessionComponent.setActiveConversationId(activeConversationId)
        sessionComponent.onToggleSidebar()
    }

    fun onSidebarDismissed() {
        sessionComponent.onDismissSidebar()
    }

    fun onSettingsButtonTapped() {
        presentSettings()
    }

    fun presentSettings() {
        if (settingComponent == null) {
            settingComponent = SidePanelSettingComponent(
                componentContext = this,
                providers = providers,
                credentialStore = credentialStore,
                preferenceStore = preferenceStore,
                modelSupportsReasoning = _state.value.modelSupportsReasoning,
                selectedProviderId = _state.value.selectedProviderId,
                onClose = ::dismissSettings,
                onCredentialsChanged = {
                    onDelegate(Delegate.CredentialsChanged)
                },
                onReasoningModelChanged = {
                    onDelegate(Delegate.ReasoningModelChanged)
                },
                onProviderChanged = { providerId ->
                    _state.update { it.copy(selectedProviderId = providerId) }
                    onDelegate(Delegate.ProviderChanged(providerId))
                }
            )
        } else {
            settingComponent?.updateMirrors(
                modelSupportsReasoning = _state.value.modelSupportsReasoning,
                selectedProviderId = _state.value.selectedProviderId
            )
        }
        _state.update { it.copy(isSettingPresented = true) }
    }

    fun dismissSettings() {
        _state.update { it.copy(isSettingPresented = false) }
    }

    fun updateMirrors(modelSupportsReasoning: Boolean, selectedProviderId: String) {
        _state.update {
            it.copy(
                modelSupportsReasoning = modelSupportsReasoning,
                selectedProviderId = selectedProviderId
            )
        }
        settingComponent?.updateMirrors(modelSupportsReasoning, selectedProviderId)
    }
}
