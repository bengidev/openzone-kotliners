package io.github.bengidev.openzone.onboarding.theme

import androidx.compose.runtime.staticCompositionLocalOf
import io.github.bengidev.openzone.ui.theme.LightOpenZonePalette
import io.github.bengidev.openzone.ui.theme.OpenZonePalette

/**
 * Onboarding palette — typealias to the authoritative `OpenZonePalette`
 * (iOS-faithful graphite monochrome). Kept as a typealias (not removed)
 * so existing onboarding call-sites that import `OnboardingPalette` keep
 * compiling. All new code should import `OpenZonePalette` directly from
 * `ui.theme`.
 *
 * Migration notes:
 * - Removed `galaxyAura` token (was blue-tinted overlay, unused in code).
 * - Removed `isDark` discriminator as a top-level field (still available
 *   as `palette.isDark` on the underlying `OpenZonePalette`).
 */
typealias OnboardingPalette = OpenZonePalette

val LightOnboardingPalette: OnboardingPalette = LightOpenZonePalette
val DarkOnboardingPalette: OnboardingPalette = io.github.bengidev.openzone.ui.theme.DarkOpenZonePalette

val LocalOnboardingPalette = staticCompositionLocalOf<OnboardingPalette> { LightOnboardingPalette }
