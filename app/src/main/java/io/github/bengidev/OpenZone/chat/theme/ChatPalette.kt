package io.github.bengidev.openzone.chat.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.bengidev.openzone.ui.theme.OpenZonePalette

/**
 * Chat-specific palette tokens — **derived from the authoritative
 * `OpenZonePalette` (iOS-faithful graphite monochrome)**. The iOS Chat
 * surface uses `controlStrong` / `surfaceRaised` / `textTertiary` /
 * `accentPrimary` from `OpenZonePalette`; this data class aliases those
 * tokens into chat-readable names so the presenter layer reads
 * semantically (`userBubble`, `reasoningCard`, etc.).
 *
 * Mirrors iOS `ChatBubbleStyle` / `ChatReasoningStyle`.
 */
@Immutable
data class ChatPalette(
    val userBubble: Color,
    val userBubbleText: Color,
    val assistantBubble: Color,
    val assistantBubbleText: Color,
    /** Reasoning card fill — iOS `surfaceRaised.opacity(0.55)` over `surfaceBase`. */
    val reasoningCard: Color,
    val reasoningBorder: Color,
    val reasoningText: Color,
    val reasoningChevron: Color,
    val streamingDot: Color,
    val systemMessageText: Color,
    val messageMetaText: Color
)

val LocalChatPalette = staticCompositionLocalOf<ChatPalette> { ChatPaletteDefaults.light }

/**
 * Builds a `ChatPalette` from the active `OpenZonePalette`. All chat
 * tokens resolve to a single source of truth — iOS-faithful graphite.
 */
object ChatPaletteDefaults {

    fun fromPalette(p: OpenZonePalette): ChatPalette = ChatPalette(
        userBubble = p.controlStrong,
        userBubbleText = p.controlStrongText,
        assistantBubble = p.surfaceRaised,
        assistantBubbleText = p.textPrimary,
        // iOS applies `.opacity(0.55)` to `surfaceRaised`; pre-blend it.
        reasoningCard = blend(p.surfaceBase, p.surfaceRaised, 0.55f),
        reasoningBorder = p.textTertiary.copy(alpha = 0.12f),
        reasoningText = p.textSecondary,
        reasoningChevron = p.textTertiary,
        streamingDot = p.accentPrimary,
        systemMessageText = p.warning,
        messageMetaText = p.textTertiary
    )

    /** Static light default (pre-Compose fallback). */
    val light: ChatPalette = fromPalette(
        io.github.bengidev.openzone.ui.theme.LightOpenZonePalette
    )

    /** Static dark default. */
    val dark: ChatPalette = fromPalette(
        io.github.bengidev.openzone.ui.theme.DarkOpenZonePalette
    )

    /** Compose accessor driven by system dark/light. */
    @Composable
    @ReadOnlyComposable
    fun forCurrent(darkTheme: Boolean): ChatPalette =
        if (darkTheme) dark else light
}

/** Simple alpha-blend between two colors. */
private fun blend(a: Color, b: Color, t: Float): Color = Color(
    red = a.red * (1 - t) + b.red * t,
    green = a.green * (1 - t) + b.green * t,
    blue = a.blue * (1 - t) + b.blue * t,
    alpha = a.alpha * (1 - t) + b.alpha * t
)

object ChatTheme {
    val palette: ChatPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalChatPalette.current
}
