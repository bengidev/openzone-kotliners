package io.github.bengidev.openzone.home.domain

import kotlin.math.roundToInt

data class ComposerContextUsage(
    val usedTokens: Int,
    val tokenLimit: Int
) {
    val usedFraction: Float
        get() {
            if (tokenLimit <= 0) return 0f
            return (usedTokens.toFloat() / tokenLimit).coerceIn(0f, 1f)
        }

    val usedPercent: Int
        get() = (usedFraction * 100f).roundToInt()

    val remainingPercent: Int
        get() = (100 - usedPercent).coerceAtLeast(0)

    val usedTokensLabel: String
        get() = compactTokenLabel(usedTokens)

    val tokenLimitLabel: String
        get() = compactTokenLabel(tokenLimit)

    private fun compactTokenLabel(tokens: Int): String =
        if (tokens >= 1_000) "${tokens / 1_000}k" else tokens.toString()
}
