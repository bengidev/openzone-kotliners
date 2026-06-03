package com.example.onboarding.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Radius tokens for OpenZone onboarding.
 * Small, intentional radius system.
 */
@Immutable
data class OnboardingRadius(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val pill: Dp
)

val DefaultOnboardingRadius = OnboardingRadius(
    xs = 6.dp,
    sm = 8.dp,
    md = 16.dp,
    lg = 20.dp,
    pill = 999.dp
)

val LocalOnboardingRadius = staticCompositionLocalOf { DefaultOnboardingRadius }
