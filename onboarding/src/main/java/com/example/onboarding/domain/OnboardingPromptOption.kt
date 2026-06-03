package com.example.onboarding.domain

/**
 * Prompt option shown in the Idea Studio visual demo.
 * Represents a preset prompt that users can select to see example interactions.
 */
data class OnboardingPromptOption(
    val label: String,
    val prompt: String
) {
    val id: String get() = prompt

    companion object {
        val samples: List<OnboardingPromptOption> = listOf(
            OnboardingPromptOption(
                label = "ASK",
                prompt = "How should I structure the memory model for this workflow?"
            ),
            OnboardingPromptOption(
                label = "WRITE",
                prompt = "Draft a concise interface view for the secure pairing step."
            ),
            OnboardingPromptOption(
                label = "EXPLORE",
                prompt = "Compare state actions for queued prompts and reasoning controls."
            )
        )
    }
}
