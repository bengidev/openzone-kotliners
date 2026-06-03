package com.example.onboarding.presenter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.onboarding.application.OnboardingState
import com.example.onboarding.domain.OnboardingPage
import com.example.onboarding.domain.OnboardingPageType
import com.example.onboarding.presenter.visuals.EncryptedPairingVisualView
import com.example.onboarding.presenter.visuals.IdeaStudioVisualView
import com.example.onboarding.presenter.visuals.PromptQueueVisualView
import com.example.onboarding.presenter.visuals.ReasoningControlVisualView
import com.example.onboarding.presenter.visuals.WorkspaceReadyVisualView

/**
 * Factory dispatcher that routes page types to their corresponding visual demo views.
 */
@Composable
fun PageVisualFactory(
    page: OnboardingPage,
    state: OnboardingState,
    appeared: Boolean,
    onPromptChipTapped: (Int) -> Unit,
    onPairingToggleTapped: () -> Unit,
    onReasoningLevelChanged: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    when (page.type) {
        OnboardingPageType.EncryptedPairing -> {
            EncryptedPairingVisualView(
                pairingConfirmed = state.demoState.pairingConfirmed,
                onTogglePairing = onPairingToggleTapped,
                onActionButtonClick = onPairingToggleTapped,
                modifier = modifier
            )
        }

        OnboardingPageType.IdeaStudio -> {
            IdeaStudioVisualView(
                selectedPromptIndex = state.demoState.selectedPromptIndex,
                onPromptSelected = onPromptChipTapped,
                appeared = appeared,
                modifier = modifier
            )
        }

        OnboardingPageType.PromptQueue -> {
            PromptQueueVisualView(
                queuedPromptCount = state.demoState.queuedPromptCount,
                appeared = appeared,
                modifier = modifier
            )
        }

        OnboardingPageType.ReasoningControl -> {
            ReasoningControlVisualView(
                reasoningLevel = state.demoState.reasoningLevel.toFloat(),
                onReasoningLevelChanged = { onReasoningLevelChanged(it.toDouble()) },
                appeared = appeared,
                modifier = modifier
            )
        }

        OnboardingPageType.WorkspaceReady -> {
            WorkspaceReadyVisualView(
                page = page,
                appeared = appeared,
                modifier = modifier
            )
        }
    }
}
