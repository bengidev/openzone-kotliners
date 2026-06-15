package io.github.bengidev.openzone.chat.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Chat-specific typography layered on top of `HomeTypography`.
 * Mirrors iOS `ChatTypography` — messages use 15pt sans, reasoning
 * uses 12pt monospaced, and timestamps use 10pt sans.
 */
@Immutable
data class ChatTypography(
    val messageBody: TextStyle,
    val reasoningBody: TextStyle,
    val reasoningHeader: TextStyle,
    val messageMeta: TextStyle,
    val systemMessage: TextStyle,
    val errorTitle: TextStyle,
    val errorBody: TextStyle,
    val errorAction: TextStyle
)

private val Sans = FontFamily.SansSerif
private val Mono = FontFamily.Monospace

object ChatTypographyDefaults {
    val default: ChatTypography = ChatTypography(
        messageBody = TextStyle(
            fontFamily = Sans,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        reasoningBody = TextStyle(
            fontFamily = Mono,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp
        ),
        reasoningHeader = TextStyle(
            fontFamily = Sans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 15.sp
        ),
        messageMeta = TextStyle(
            fontFamily = Sans,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            lineHeight = 13.sp
        ),
        systemMessage = TextStyle(
            fontFamily = Sans,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        errorTitle = TextStyle(
            fontFamily = Sans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp
        ),
        errorBody = TextStyle(
            fontFamily = Sans,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp
        ),
        errorAction = TextStyle(
            fontFamily = Sans,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 16.sp
        )
    )
}

val LocalChatTypography = staticCompositionLocalOf<ChatTypography> { ChatTypographyDefaults.default }
