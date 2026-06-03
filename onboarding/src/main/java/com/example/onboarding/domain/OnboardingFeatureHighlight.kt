package com.example.onboarding.domain

import androidx.annotation.DrawableRes

/**
 * Feature highlight shown in the footer of onboarding pages.
 * Displays a compact icon + title + detail to reinforce key capabilities.
 */
data class OnboardingFeatureHighlight(
    val title: String,
    val detail: String,
    @DrawableRes val iconRes: Int
) {
    val id: String get() = title
}
