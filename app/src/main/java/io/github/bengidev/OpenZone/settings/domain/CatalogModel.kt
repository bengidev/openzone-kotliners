package io.github.bengidev.openzone.settings.domain

/**
 * A selectable model in the Settings catalog. Pure data — no Android/Compose
 * imports (domain layer). Mirrors the iOS settings model descriptor.
 *
 * @property id provider model identifier sent as `ChatRequest.modelId`
 *   (e.g. `"deepseek/deepseek-chat-v3-0324:free"`).
 * @property displayName human-facing label shown in the picker.
 * @property providerId stable [io.github.bengidev.openzone.shared.networking.ChatProvider.id]
 *   this model belongs to.
 * @property isFree whether the model is on the provider's free tier; the
 *   curated fallback catalog ships free models only.
 * @property description short one-line capability hint shown under the name.
 */
data class CatalogModel(
    val id: String,
    val displayName: String,
    val providerId: String,
    val isFree: Boolean = true,
    val description: String = ""
)
