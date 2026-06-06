package io.github.bengidev.openzone.home.theme

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.bengidev.openzone.ui.theme.LightOpenZonePalette
import io.github.bengidev.openzone.ui.theme.OpenZonePalette

/**
 * Home palette — typealias to the authoritative `OpenZonePalette`
 * (iOS-faithful graphite monochrome).
 *
 * Kept as a typealias (not removed) so existing Home call-sites that
 * import `HomePalette` keep compiling. All new code should import
 * `OpenZonePalette` directly from `ui.theme`.
 */
typealias HomePalette = OpenZonePalette

val LightHomePalette: HomePalette = LightOpenZonePalette
val DarkHomePalette: HomePalette = io.github.bengidev.openzone.ui.theme.DarkOpenZonePalette

val LocalHomePalette = staticCompositionLocalOf<HomePalette> { LightHomePalette }
