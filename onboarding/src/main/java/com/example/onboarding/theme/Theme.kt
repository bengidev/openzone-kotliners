package com.example.onboarding.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * OpenZone onboarding theme.
 * Provides design tokens (palette, typography, spacing, radius) via CompositionLocals.
 */
@Composable
fun OpenZoneOnboardingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkOnboardingPalette else LightOnboardingPalette

    CompositionLocalProvider(
        LocalOnboardingPalette provides palette,
        LocalOnboardingTypography provides DefaultOnboardingTypography,
        LocalOnboardingSpacing provides DefaultOnboardingSpacing,
        LocalOnboardingRadius provides DefaultOnboardingRadius,
        content = content
    )
}

/**
 * Convenience accessors for design tokens.
 */
object OnboardingTheme {
    val palette: OnboardingPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalOnboardingPalette.current

    val typography: OnboardingTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalOnboardingTypography.current

    val spacing: OnboardingSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalOnboardingSpacing.current

    val radius: OnboardingRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalOnboardingRadius.current
}
