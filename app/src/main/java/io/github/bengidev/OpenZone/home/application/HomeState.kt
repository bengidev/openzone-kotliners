package io.github.bengidev.openzone.home.application

import io.github.bengidev.openzone.home.domain.ComposerContextUsage
import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.shared.networking.ChatModel

/**
 * UI state for the home composer shell.
 *
 * Model identity is the dynamic [selectedModelId] string (persisted in the
 * shared preference store), not a closed enum. [availableModels] is the hybrid
 * catalog the composer popup offers (live cache or curated fallback). The
 * composer model popup filters this list via [modelSearchQuery] (debounced into
 * [debouncedModelQuery]) and [modelFilterFreeOnly].
 *
 * `ComposerSpeedMode` remains a purely cosmetic composer affordance and is
 * deliberately excluded from the provider request path (see `ChatRequest`).
 */
data class HomeState(
    val draftMessage: String = "",
    val isSending: Boolean = false,
    val availableModels: List<ChatModel> = emptyList(),
    val selectedModelId: String? = null,
    val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.High,
    val speedMode: ComposerSpeedMode = ComposerSpeedMode.Standard,
    val contextUsage: ComposerContextUsage = ComposerContextUsage(
        usedTokens = 107_000,
        tokenLimit = 258_000
    ),
    val isContextUsagePresented: Boolean = false,
    val isModelPopupPresented: Boolean = false,
    val modelSearchQuery: String = "",
    val debouncedModelQuery: String = "",
    val modelFilterFreeOnly: Boolean = false,
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

    /** The currently selected model resolved against the available catalog. */
    val selectedModel: ChatModel?
        get() = availableModels.firstOrNull { it.id == selectedModelId }

    /** Human-facing label for the composer model chip. */
    val selectedModelTitle: String
        get() = selectedModel?.displayName
            ?: selectedModelId
            ?: "Select model"

    /** The catalog filtered by the debounced search query and free-tier toggle. */
    val filteredModels: List<ChatModel>
        get() = availableModels.filter { model ->
            val matchesFree = !modelFilterFreeOnly || model.isFree
            val q = debouncedModelQuery.trim()
            val matchesQuery = q.isBlank() ||
                model.displayName.contains(q, ignoreCase = true) ||
                model.id.contains(q, ignoreCase = true)
            matchesFree && matchesQuery
        }
}
