package io.github.bengidev.openzone.home.application

import io.github.bengidev.openzone.home.domain.ComposerContextUsage
import io.github.bengidev.openzone.home.domain.ComposerModelOption
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode

data class HomeState(
    val draftMessage: String = "",
    val isSending: Boolean = false,
    val selectedModel: ComposerModelOption = ComposerModelOption.Gpt54,
    val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.High,
    val speedMode: ComposerSpeedMode = ComposerSpeedMode.Standard,
    val contextUsage: ComposerContextUsage = ComposerContextUsage(
        usedTokens = 107_000,
        tokenLimit = 258_000
    ),
    val isContextUsagePresented: Boolean = false
) {
    val canSend: Boolean
        get() = draftMessage.trim().isNotEmpty() && !isSending
}
