package io.github.bengidev.openzone.home.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import io.github.bengidev.openzone.home.domain.ComposerModelOption
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode

/**
 * Decompose component for the home welcome + composer shell.
 * Mirrors iOS MainChat welcome state; chat thread wiring comes later.
 */
class HomeComponent(
    componentContext: ComponentContext,
    private val onSidebarToggle: () -> Unit = {}
) : ComponentContext by componentContext {

    private val _state = MutableValue(HomeState())
    val state: Value<HomeState> = _state

    fun onSidebarToggleTapped() {
        onSidebarToggle()
    }

    fun onDraftMessageChanged(text: String) {
        _state.update { it.copy(draftMessage = text) }
    }

    fun onSendTapped() {
        val current = _state.value
        if (!current.canSend) return
        _state.update { it.copy(draftMessage = "", isSending = false) }
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
}
