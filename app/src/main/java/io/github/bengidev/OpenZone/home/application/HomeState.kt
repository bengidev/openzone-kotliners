package io.github.bengidev.openzone.home.application

import io.github.bengidev.openzone.chat.domain.ChatConversation
import io.github.bengidev.openzone.home.domain.ComposerContextUsage
import io.github.bengidev.openzone.home.domain.ComposerSpeedMode
import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel

/**
 * UI state for the home composer shell.
 *
 * Model identity is the dynamic [selectedModelId] string (persisted in the
 * shared preference store), not a closed enum. [availableModels] is the hybrid
 * catalog the composer popup offers (live cache or curated fallback). The
 * composer model popup filters this list via [modelSearchQuery] (debounced into
 * [debouncedModelQuery]) and [modelFilterFreeOnly].
 *
 * [reasoningLevel] defaults to [ComposerReasoningLevel.Off]. The reasoning
 * indicator and effort control are shown only when [selectedModelSupportsReasoning]
 * is true — i.e. the currently selected model has [ChatModel.supportsReasoning]
 * set. Changing the level via the composer chip or Settings both write through
 * to the shared [ProviderPreferenceStore] so the value is durable.
 *
 * `ComposerSpeedMode` remains a purely cosmetic composer affordance and is
 * deliberately excluded from the provider request path (see `ChatRequest`).
 */
data class HomeState(
    val draftMessage: String = "",
    val isSending: Boolean = false,
    val availableModels: List<ChatModel> = emptyList(),
    val selectedModelId: String? = null,
    val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.Off,
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
    val isSidebarPresented: Boolean = false,
    val conversations: List<ChatConversation> = emptyList(),
    val isChatConfigured: Boolean = false,
    val hasApiKey: Boolean = false,
    val hasLoadedPreference: Boolean = false
) {
    val canSend: Boolean
        get() = draftMessage.trim().isNotEmpty() && !isSending && isChatConfigured

    val showMissingApiKeyHint: Boolean
        get() = hasLoadedPreference && !hasApiKey

    val selectedModel: ChatModel?
        get() = availableModels.firstOrNull { it.id == selectedModelId }

    val selectedModelTitle: String
        get() = selectedModel?.displayName
            ?: selectedModelId
            ?: "Select model"

    val selectedModelSupportsReasoning: Boolean
        get() = selectedModel?.supportsReasoning == true

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
