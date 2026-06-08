package io.github.bengidev.openzone.shared.networking

import io.github.bengidev.openzone.home.domain.ComposerReasoningLevel

/**
 * Persisted, non-secret provider/model/reasoning selection. Feature-neutral
 * primitive shared between Settings (writes) and Chat/Home (reads) — contains
 * no secrets and no Android/Compose types.
 *
 * @property providerId stable [ChatProvider.id] the user selected.
 * @property modelId provider model identifier, or `null` when no model chosen.
 * @property reasoningLevel the persisted reasoning effort level; defaults to
 *   [ComposerReasoningLevel.Off] so new installs never accidentally send a
 *   reasoning parameter to non-reasoning models.
 */
data class ProviderPreference(
    val providerId: String,
    val modelId: String? = null,
    val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.Off
)
