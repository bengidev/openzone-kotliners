package io.github.bengidev.openzone.chat.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Wraps chat UI in the chat-specific theme tokens. The chat palette is
 * iOS-faithful (graphite monochrome) regardless of the host app's accent.
 *
 * Mirrors iOS's pattern of injecting `OpenZonePalette` into the SwiftUI
 * environment for any chat-using surface.
 */
@Composable
fun OpenZoneChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = ChatPaletteDefaults.forCurrent(darkTheme)
    CompositionLocalProvider(
        LocalChatPalette provides palette,
        LocalChatTypography provides ChatTypographyDefaults.default
    ) {
        content()
    }
}
