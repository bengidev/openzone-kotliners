package io.github.bengidev.openzone.home.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class HomeSpacing(
    val space1: Dp,
    val space2: Dp,
    val space3: Dp,
    val space4: Dp,
    val space5: Dp,
    val space6: Dp
)

val DefaultHomeSpacing = HomeSpacing(
    space1 = 6.dp,
    space2 = 10.dp,
    space3 = 14.dp,
    space4 = 18.dp,
    space5 = 22.dp,
    space6 = 28.dp
)

val LocalHomeSpacing = staticCompositionLocalOf { DefaultHomeSpacing }
