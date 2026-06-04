package io.github.bengidev.openzone.home.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class HomePalette(
    val isDark: Boolean,
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val inverseSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val strongBorder: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentText: Color,
    val success: Color,
    val warning: Color,
    val primaryActionFill: Color,
    val primaryActionText: Color,
    val orbTint: Color
)

val LightHomePalette = HomePalette(
    isDark = false,
    background = HomeBackgroundLight,
    backgroundSecondary = HomeBackgroundSecondaryLight,
    surface = HomeSurfaceLight,
    elevatedSurface = HomeElevatedSurfaceLight,
    inverseSurface = HomeInverseSurfaceLight,
    textPrimary = HomeTextPrimaryLight,
    textSecondary = HomeTextSecondaryLight,
    textMuted = HomeTextMutedLight,
    border = HomeBorderLight,
    strongBorder = HomeStrongBorderLight,
    accent = HomeAccentLight,
    accentSoft = HomeAccentSoftLight,
    accentText = HomeAccentTextLight,
    success = HomeSuccessLight,
    warning = HomeWarningLight,
    primaryActionFill = HomePrimaryActionFillLight,
    primaryActionText = HomePrimaryActionTextLight,
    orbTint = HomeOrbTintLight
)

val DarkHomePalette = HomePalette(
    isDark = true,
    background = HomeBackgroundDark,
    backgroundSecondary = HomeBackgroundSecondaryDark,
    surface = HomeSurfaceDark,
    elevatedSurface = HomeElevatedSurfaceDark,
    inverseSurface = HomeInverseSurfaceDark,
    textPrimary = HomeTextPrimaryDark,
    textSecondary = HomeTextSecondaryDark,
    textMuted = HomeTextMutedDark,
    border = HomeBorderDark,
    strongBorder = HomeStrongBorderDark,
    accent = HomeAccentDark,
    accentSoft = HomeAccentSoftDark,
    accentText = HomeAccentTextDark,
    success = HomeSuccessDark,
    warning = HomeWarningDark,
    primaryActionFill = HomePrimaryActionFillDark,
    primaryActionText = HomePrimaryActionTextDark,
    orbTint = HomeOrbTintDark
)

val LocalHomePalette = staticCompositionLocalOf { LightHomePalette }
