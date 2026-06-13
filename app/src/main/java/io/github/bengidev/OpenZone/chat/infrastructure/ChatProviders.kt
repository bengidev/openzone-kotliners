package io.github.bengidev.openzone.chat.infrastructure

import io.github.bengidev.openzone.shared.externals.networking.AuthScheme
import io.github.bengidev.openzone.shared.externals.networking.ChatProvider

/**
 * Built-in [ChatProvider] definitions. OpenRouter is the first configured
 * provider for OpenZone (issue #2).
 */
object ChatProviders {

    /**
     * OpenRouter — OpenAI-compatible aggregator. Bearer auth; attribution
     * headers (`HTTP-Referer`, `X-Title`) identify the app per OpenRouter's
     * guidelines and are safe to send on every request (no secrets).
     */
    val openRouter: ChatProvider = ChatProvider(
        id = "openrouter",
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        authScheme = AuthScheme.Bearer,
        defaultHeaders = mapOf(
            "HTTP-Referer" to "https://github.com/bengidev/openzone-kotliners",
            "X-Title" to "OpenZone"
        )
    )

    /** All providers known at build time. */
    val all: List<ChatProvider> = listOf(openRouter)
}
