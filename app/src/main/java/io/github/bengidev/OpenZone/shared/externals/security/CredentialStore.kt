package io.github.bengidev.openzone.shared.externals.security

/**
 * Reads provider credentials at call time. Implementations are backed by
 * secure storage (e.g. EncryptedSharedPreferences — added in a later slice).
 *
 * Read at call time, never captured at construction, so a key entered after the
 * client is built is picked up on the next request and a cleared key takes
 * effect immediately. Mirrors the iOS credential-store seam.
 */
interface CredentialStore {
    /**
     * Returns the secret for [providerId], or `null` if none is stored.
     * Called on each request — implementations should be cheap / cached.
     */
    fun secretFor(providerId: String): String?
}
