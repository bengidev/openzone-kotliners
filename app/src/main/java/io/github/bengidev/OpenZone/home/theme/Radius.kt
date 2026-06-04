package io.github.bengidev.openzone.home.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class HomeRadius(
    val chip: Dp,
    val composer: Dp,
    val pill: Dp
)

val DefaultHomeRadius = HomeRadius(
    chip = 16.dp,
    composer = 28.dp,
    pill = 999.dp
)

val LocalHomeRadius = staticCompositionLocalOf { DefaultHomeRadius }
