package io.github.bengidev.openzone.settings.application

import io.github.bengidev.openzone.settings.domain.CatalogModel
import io.github.bengidev.openzone.shared.networking.ChatProvider

/**
 * UI state for the Settings surface. Holds no secret value — only whether a key
 * is present ([hasApiKey]) and the in-progress [apiKeyDraft] the user is typing.
 * The actual stored secret never enters state and is never rendered back.
 *
 * @property providers selectable providers (OpenRouter-first).
 * @property selectedProviderId currently selected provider id.
 * @property models curated catalog models for the selected provider.
 * @property selectedModelId currently selected model id, or `null` if unset.
 * @property apiKeyDraft transient text-field contents for key entry.
 * @property hasApiKey whether a key is currently stored for the selected provider.
 * @property isLoaded whether initial persisted state has been read.
 */
data class SettingsState(
    val providers: List<ChatProvider> = emptyList(),
    val selectedProviderId: String? = null,
    val models: List<CatalogModel> = emptyList(),
    val selectedModelId: String? = null,
    val apiKeyDraft: String = "",
    val hasApiKey: Boolean = false,
    val isLoaded: Boolean = false
) {
    /** The selected provider descriptor, if resolvable. */
    val selectedProvider: ChatProvider?
        get() = providers.firstOrNull { it.id == selectedProviderId }

    /** Whether the draft field holds a non-blank, persistable key. */
    val canSaveKey: Boolean
        get() = apiKeyDraft.isNotBlank()
}
