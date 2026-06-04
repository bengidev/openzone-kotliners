package io.github.bengidev.openzone.onboarding.application

import io.github.bengidev.openzone.onboarding.domain.OnboardingPage
import io.github.bengidev.openzone.onboarding.domain.OnboardingPromptOption
import io.github.bengidev.openzone.onboarding.domain.OnboardingQueueItem

/**
 * Immutable state of the onboarding flow.
 * Contains current page index, completion status, and demo interaction state.
 */
data class OnboardingState(
    val currentPage: Int = 0,
    val isFinished: Boolean = false,
    val demoState: DemoState = DemoState()
) {
    val pages: List<OnboardingPage> = OnboardingPage.all
    val totalPages: Int get() = pages.size
    val isLastPage: Boolean get() = currentPage >= totalPages - 1
    val currentPageData: OnboardingPage
        get() = pages[currentPage.coerceIn(0, totalPages - 1)]

    /**
     * Demo interaction state for visual demonstrations.
     */
    data class DemoState(
        val selectedPromptIndex: Int = 0,
        val queuedPromptCount: Int = 2,
        val reasoningLevel: Double = 0.62,
        val pairingConfirmed: Boolean = true,
        val promptOptions: List<OnboardingPromptOption> = OnboardingPromptOption.samples,
        val queueItems: List<OnboardingQueueItem> = OnboardingQueueItem.samples
    )
}
