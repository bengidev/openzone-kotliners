package io.github.bengidev.openzone.settings.infrastructure

import io.github.bengidev.openzone.shared.externals.security.MutableCredentialStore

/**
 * In-memory [MutableCredentialStore] test double. Mirrors the encrypted store's
 * contract (blank writes clear, blank reads return null) without touching the
 * Android Keystore, so it can be exercised in plain JVM unit tests.
 */
class InMemoryCredentialStore : MutableCredentialStore {
    private val secrets = mutableMapOf<String, String>()

    override fun secretFor(providerId: String): String? =
        secrets[providerId]?.takeIf { it.isNotBlank() }

    override fun setSecret(providerId: String, secret: String) {
        val trimmed = secret.trim()
        if (trimmed.isEmpty()) {
            clear(providerId)
            return
        }
        secrets[providerId] = trimmed
    }

    override fun clear(providerId: String) {
        secrets.remove(providerId)
    }
}
