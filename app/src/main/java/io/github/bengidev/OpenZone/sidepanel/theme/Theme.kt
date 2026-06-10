package io.github.bengidev.openzone.sidepanel.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.bengidev.openzone.ui.theme.DarkOpenZonePalette
import io.github.bengidev.openzone.ui.theme.LightOpenZonePalette
import io.github.bengidev.openzone.ui.theme.OpenZonePalette

private val LocalSidePanelPalette = staticCompositionLocalOf<OpenZonePalette> { LightOpenZonePalette }

@Composable
fun OpenZoneSidePanelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkOpenZonePalette else LightOpenZonePalette
    CompositionLocalProvider(LocalSidePanelPalette provides palette) {
        content()
    }
}

object SidePanelTheme {
    val palette: OpenZonePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalSidePanelPalette.current
}
