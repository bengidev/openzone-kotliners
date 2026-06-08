package io.github.bengidev.openzone.settings.application

import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.networking.ChatModel
import io.github.bengidev.openzone.shared.networking.ChatProvider

/**
 * UI state for the Settings surface. Holds no secret value — only whether a
 * key is present ([hasApiKey]) and the in-progress [apiKeyDraft] the user is
 * typing. The actual stored secret never enters state and is never rendered back.
 *
 * [reasoningLevel] is the persisted reasoning effort. The reasoning section is
 * shown only when [selectedModelSupportsReasoning] is true — i.e. the currently
 * selected model has [ChatModel.supportsReasoning] set.
 *
 * @property providers selectable providers (OpenRouter-first).
 * @property selectedProviderId currently selected provider id.
 * @property models catalog models for the selected provider (live or curated fallback).
 * @property selectedModelId currently selected model id, or `null` if unset.
 * @property reasoningLevel persisted reasoning effort level.
 * @property apiKeyDraft transient text-field contents for key entry.
 * @property hasApiKey whether a key is currently stored for the selected provider.
 * @property isLoaded whether initial persisted state has been read.
 * @property isLoadingModels whether a live catalog fetch is in progress.
 */
data class SettingsState(
    val providers: List<ChatProvider> = emptyList(),
    val selectedProviderId: String? = null,
    val models: List<ChatModel> = emptyList(),
    val selectedModelId: String? = null,
    val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.Off,
    val apiKeyDraft: String = "",
    val hasApiKey: Boolean = false,
    val isLoaded: Boolean = false,
    val isLoadingModels: Boolean = false
) {
    /** The selected provider descriptor, if resolvable. */
    val selectedProvider: ChatProvider?
        get() = providers.firstOrNull { it.id == selectedProviderId }

    /** The selected model descriptor, if resolvable against the catalog. */
    val selectedModel: ChatModel?
        get() = models.firstOrNull { it.id == selectedModelId }

    /**
     * Whether the selected model supports reasoning. Controls visibility of the
     * reasoning effort section in Settings.
     */
    val selectedModelSupportsReasoning: Boolean
        get() = selectedModel?.supportsReasoning == true

    /** Whether the draft field holds a non-blank, persistable key. */
    val canSaveKey: Boolean
        get() = apiKeyDraft.isNotBlank()
}
