package io.github.bengidev.openzone.home.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class HomeTypography(
    val welcomeTitle: TextStyle,
    val welcomeCaption: TextStyle,
    val composerInput: TextStyle,
    val chipLabel: TextStyle,
    val contextPercent: TextStyle
)

private val Mono = FontFamily.Monospace
private val Sans = FontFamily.SansSerif

val DefaultHomeTypography = HomeTypography(
    // iOS MainChatWelcomeView: 28pt semibold monospaced
    welcomeTitle = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    welcomeCaption = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    ),
    composerInput = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    chipLabel = TextStyle(
        fontFamily = Sans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 15.sp
    ),
    contextPercent = TextStyle(
        fontFamily = Mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 8.sp,
        lineHeight = 10.sp
    )
)

val LocalHomeTypography = staticCompositionLocalOf { DefaultHomeTypography }
