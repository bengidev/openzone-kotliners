package io.github.bengidev.openzone.shared.externals.networking

/**
 * Pure-data descriptor for a chat provider. Feature-neutral primitive shared
 * across features — contains no secrets and no Android/Compose types.
 *
 * Mirrors the iOS `ChatProvider` descriptor.
 *
 * @property id stable identifier (e.g. `"openrouter"`); also the credential-store key.
 * @property displayName human-facing name shown in settings.
 * @property baseUrl provider API root, no trailing slash (e.g. `https://openrouter.ai/api/v1`).
 * @property authScheme how credentials attach to outgoing requests.
 * @property defaultHeaders static headers sent on every request (e.g. attribution).
 */
data class ChatProvider(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val authScheme: AuthScheme,
    val defaultHeaders: Map<String, String> = emptyMap()
) {
    /** Full URL for the chat-completions endpoint. */
    val chatCompletionsUrl: String
        get() = "${baseUrl.trimEnd('/')}/chat/completions"
}
