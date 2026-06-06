package io.github.bengidev.openzone.ui.theme

import androidx.compose.runtime.compositionLocalOf

/** Theme preference — mirrors iOS `AppTheme` (system → light → dark → system). */
enum class AppTheme {
    System,
    Light,
    Dark;

    fun resolveDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }

    val next: AppTheme
        get() = when (this) {
            System -> Light
            Light -> Dark
            Dark -> System
        }
}

val LocalAppTheme = compositionLocalOf { AppTheme.System }
