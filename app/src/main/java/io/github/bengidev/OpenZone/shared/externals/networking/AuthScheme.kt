package io.github.bengidev.openzone.shared.externals.networking

/**
 * Authentication scheme a [ChatProvider] uses when issuing requests.
 *
 * Feature-neutral: this describes *how* a credential is attached to a request,
 * not where the secret comes from (see [CredentialStore]).
 */
sealed interface AuthScheme {
    /** No authentication header is attached. */
    data object None : AuthScheme

    /**
     * Bearer token in the `Authorization` header: `Authorization: Bearer <key>`.
     * OpenAI, OpenRouter, and most OpenAI-compatible providers use this.
     */
    data object Bearer : AuthScheme
}
