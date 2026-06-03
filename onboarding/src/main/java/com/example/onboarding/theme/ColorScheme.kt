package com.example.onboarding.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color palette for OpenZone onboarding.
 * Wraps all design tokens with semantic names for easy consumption.
 */
@Immutable
data class OnboardingPalette(
    val surfaceBase: Color,
    val surfacePaper: Color,
    val surfaceRaised: Color,
    val surfaceSubtle: Color,
    val surfaceGalaxyTint: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val lineSoft: Color,
    val lineStrong: Color,
    val accentPrimary: Color,
    val accentDeep: Color,
    val accentSoft: Color,
    val controlStrong: Color,
    val controlStrongText: Color,
    val danger: Color,
    val success: Color,
    val warning: Color,
    val galaxyAura: Color,
    val isDark: Boolean
)

val LightOnboardingPalette = OnboardingPalette(
    surfaceBase = SurfaceBaseLight,
    surfacePaper = SurfacePaperLight,
    surfaceRaised = SurfaceRaisedLight,
    surfaceSubtle = SurfaceSubtleLight,
    surfaceGalaxyTint = SurfaceGalaxyTintLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    lineSoft = LineSoftLight,
    lineStrong = LineStrongLight,
    accentPrimary = AccentPrimaryLight,
    accentDeep = AccentDeepLight,
    accentSoft = AccentSoftLight,
    controlStrong = ControlStrongLight,
    controlStrongText = ControlStrongTextLight,
    danger = DangerLight,
    success = SuccessLight,
    warning = WarningLight,
    galaxyAura = GalaxyAuraLight,
    isDark = false
)

val DarkOnboardingPalette = OnboardingPalette(
    surfaceBase = SurfaceBaseDark,
    surfacePaper = SurfacePaperDark,
    surfaceRaised = SurfaceRaisedDark,
    surfaceSubtle = SurfaceSubtleDark,
    surfaceGalaxyTint = SurfaceGalaxyTintDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textTertiary = TextTertiaryDark,
    lineSoft = LineSoftDark,
    lineStrong = LineStrongDark,
    accentPrimary = AccentPrimaryDark,
    accentDeep = AccentDeepDark,
    accentSoft = AccentSoftDark,
    controlStrong = ControlStrongDark,
    controlStrongText = ControlStrongTextDark,
    danger = DangerDark,
    success = SuccessDark,
    warning = WarningDark,
    galaxyAura = GalaxyAuraDark,
    isDark = true
)

val LocalOnboardingPalette = staticCompositionLocalOf { LightOnboardingPalette }
