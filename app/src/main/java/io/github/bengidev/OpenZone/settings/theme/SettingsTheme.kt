package io.github.bengidev.openzone.settings.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.bengidev.openzone.ui.theme.DarkOpenZonePalette
import io.github.bengidev.openzone.ui.theme.LightOpenZonePalette
import io.github.bengidev.openzone.ui.theme.OpenZonePalette

/**
 * Settings palette — typealias to the authoritative `OpenZonePalette`
 * (iOS-faithful graphite monochrome). No duplicate color constants; all tokens
 * resolve from the single source of truth in `ui.theme`. Matches the Home/Chat
 * convention of aliasing rather than redefining tokens.
 */
typealias SettingsPalette = OpenZonePalette

val LightSettingsPalette: SettingsPalette = LightOpenZonePalette
val DarkSettingsPalette: SettingsPalette = DarkOpenZonePalette

val LocalSettingsPalette = staticCompositionLocalOf<SettingsPalette> { LightSettingsPalette }

/**
 * Provides the Settings design tokens. Thin wrapper over the authoritative
 * palette so the Settings presenter never hardcodes colors.
 */
@Composable
fun OpenZoneSettingsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkSettingsPalette else LightSettingsPalette
    CompositionLocalProvider(
        LocalSettingsPalette provides palette,
        content = content
    )
}

object SettingsTheme {
    val palette: SettingsPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalSettingsPalette.current
}
