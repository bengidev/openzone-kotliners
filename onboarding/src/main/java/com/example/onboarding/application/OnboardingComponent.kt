package com.example.onboarding.application

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.example.onboarding.domain.OnboardingPage
import com.example.onboarding.infrastructure.OnboardingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Decompose component managing the onboarding flow state.
 * Handles page navigation, demo interactions, and persistence lifecycle.
 */
class OnboardingComponent(
    componentContext: ComponentContext,
    private val repository: OnboardingRepository,
    private val onComplete: () -> Unit
) : ComponentContext by componentContext {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _state = MutableValue(OnboardingState())
    val state: Value<OnboardingState> = _state

    init {
        checkCompletionStatus()
    }

    private fun checkCompletionStatus() {
        scope.launch {
            val isCompleted = repository.isOnboardingCompleted()
            _state.update { it.copy(isFinished = isCompleted) }
            if (isCompleted) {
                onComplete()
            }
        }
    }

    fun onNextTapped() {
        _state.update { current ->
            val nextPage = (current.currentPage + 1).coerceAtMost(current.totalPages - 1)
            current.copy(currentPage = nextPage)
        }
    }

    fun onPreviousTapped() {
        _state.update { current ->
            val prevPage = (current.currentPage - 1).coerceAtLeast(0)
            current.copy(currentPage = prevPage)
        }
    }

    fun onPageSelected(index: Int) {
        _state.update { current ->
            val safeIndex = index.coerceIn(0, current.totalPages - 1)
            current.copy(currentPage = safeIndex)
        }
    }

    fun onFinishTapped() {
        _state.update { it.copy(isFinished = true) }
        scope.launch {
            repository.completeOnboarding()
            onComplete()
        }
    }

    fun onSkipTapped() {
        _state.update { current ->
            current.copy(currentPage = current.totalPages - 1)
        }
    }

    // Demo interaction handlers
    fun onPromptChipTapped(index: Int) {
        _state.update { current ->
            val safeIndex = index.coerceIn(0, current.demoState.promptOptions.size - 1)
            current.copy(
                demoState = current.demoState.copy(selectedPromptIndex = safeIndex)
            )
        }
    }

    fun onAddQueuedPromptTapped() {
        _state.update { current ->
            val newCount = if (current.demoState.queuedPromptCount >= current.demoState.queueItems.size) {
                2
            } else {
                current.demoState.queuedPromptCount + 1
            }
            current.copy(
                demoState = current.demoState.copy(queuedPromptCount = newCount)
            )
        }
    }

    fun onReasoningLevelChanged(value: Double) {
        _state.update { current ->
            val clampedValue = value.coerceIn(0.0, 1.0)
            current.copy(
                demoState = current.demoState.copy(reasoningLevel = clampedValue)
            )
        }
    }

    fun onPairingToggleTapped() {
        _state.update { current ->
            current.copy(
                demoState = current.demoState.copy(pairingConfirmed = !current.demoState.pairingConfirmed)
            )
        }
    }

    fun onDestroy() {
        scope.cancel()
    }
}
