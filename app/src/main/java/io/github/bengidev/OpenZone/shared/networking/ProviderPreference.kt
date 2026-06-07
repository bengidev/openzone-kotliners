package io.github.bengidev.openzone.shared.networking

/**
 * Persisted, non-secret provider/model selection. Feature-neutral primitive
 * shared between Settings (writes) and Chat/Home (reads) — contains no secrets
 * and no Android/Compose types.
 *
 * @property providerId stable [ChatProvider.id] the user selected.
 * @property modelId provider model identifier (e.g. `"deepseek/deepseek-chat"`),
 *   or `null` when no model has been chosen yet.
 */
data class ProviderPreference(
    val providerId: String,
    val modelId: String? = null
)
