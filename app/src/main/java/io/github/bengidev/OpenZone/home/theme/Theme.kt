package io.github.bengidev.openzone.home.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

@Composable
fun OpenZoneHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkHomePalette else LightHomePalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.accentText,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.elevatedSurface,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.accentText,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.elevatedSurface,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.border
        )
    }

    CompositionLocalProvider(
        LocalHomePalette provides palette,
        LocalHomeTypography provides DefaultHomeTypography,
        LocalHomeSpacing provides DefaultHomeSpacing,
        LocalHomeRadius provides DefaultHomeRadius
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

object HomeTheme {
    val palette: HomePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalHomePalette.current

    val typography: HomeTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalHomeTypography.current

    val spacing: HomeSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalHomeSpacing.current

    val radius: HomeRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalHomeRadius.current
}

/** Transparent TextField inside composer card. */
@Composable
fun homeComposerTextFieldColors() =
    androidx.compose.material3.TextFieldDefaults.colors(
        focusedTextColor = HomeTheme.palette.textPrimary,
        unfocusedTextColor = HomeTheme.palette.textPrimary,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = HomeTheme.palette.accent,
        focusedPlaceholderColor = HomeTheme.palette.textMuted,
        unfocusedPlaceholderColor = HomeTheme.palette.textMuted
    )
