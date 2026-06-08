package io.github.bengidev.openzone.home.application

import io.github.bengidev.openzone.chat.domain.ChatConversation
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
    /**
     * Whether the sidebar conversation-history drawer is open (issue #8).
     * Settings remains a separate top-bar sheet and is never relocated here.
     */
    val isSidebarPresented: Boolean = false,
    /** Persisted conversations shown in the sidebar, most-recent first. */
    val conversations: List<ChatConversation> = emptyList(),
    /**
     * Whether the chat path is ready to send: a credential is stored for the
     * selected provider and a model has been chosen (both via Settings).
     * Derived by [HomeComponent] from the credential + preference stores.
     */
    val isChatConfigured: Boolean = false,
    /**
     * Whether an API key is stored for the selected provider, independent of
     * model selection. Drives the composer's "Add an API key" hint. Mirrors
     * iOS `HomeFeature` `hasAPIKey`.
     */
    val hasApiKey: Boolean = false,
    /**
     * Whether [ProviderPreferenceStore.preferenceFlow] has emitted at least once.
     * Prevents the missing-key hint from flashing before credentials are known.
     */
    val hasLoadedPreference: Boolean = false
) {
    val canSend: Boolean
        get() = draftMessage.trim().isNotEmpty() && !isSending && isChatConfigured

    /** Show the composer missing-key banner only after preference state is known. */
    val showMissingApiKeyHint: Boolean
        get() = hasLoadedPreference && !hasApiKey

    /** The currently selected model resolved against the available catalog. */
    val selectedModel: ChatModel?
        get() = availableModels.firstOrNull { it.id == selectedModelId }

    /** Human-facing label for the composer model chip. */
    val selectedModelTitle: String
        get() = selectedModel?.displayName
            ?: selectedModelId
            ?: "Select model"

    /**
     * Whether the selected model supports reasoning. Controls visibility of the
     * reasoning indicator and effort control in the composer rail and Settings.
     */
    val selectedModelSupportsReasoning: Boolean
        get() = selectedModel?.supportsReasoning == true

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
