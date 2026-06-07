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
    val isContextUsagePresented: Boolean = false,
    val isSettingsPresented: Boolean = false,
    /**
     * Whether the chat path is ready to send: a credential is stored for the
     * selected provider and a model has been chosen (both via Settings).
     * Derived by [HomeComponent] from the credential + preference stores.
     */
    val isChatConfigured: Boolean = false
) {
    val canSend: Boolean
        get() = draftMessage.trim().isNotEmpty() && !isSending && isChatConfigured
}
