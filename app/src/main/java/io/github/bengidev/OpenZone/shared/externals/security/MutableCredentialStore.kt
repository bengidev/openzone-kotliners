package io.github.bengidev.openzone.shared.externals.security

import io.github.bengidev.openzone.shared.externals.security.CredentialStore

/**
 * Read-write extension of [CredentialStore]. The Chat feature depends only on
 * the read-only [CredentialStore] seam (it just needs to resolve a secret at
 * call time); the Settings feature depends on this mutable variant so it can
 * persist and clear keys. Keeping the write surface separate means Chat never
 * gains the ability to mutate credentials, and the two features stay decoupled
 * behind interfaces in `shared/networking/`.
 *
 * Implementations are backed by secure storage (EncryptedSharedPreferences).
 * Secrets are never logged and never leave the device.
 */
interface MutableCredentialStore : CredentialStore {
    /**
     * Persists [secret] for [providerId], overwriting any existing value.
     * A blank secret is treated as a clear (see [clear]).
     */
    fun setSecret(providerId: String, secret: String)

    /** Removes any stored secret for [providerId]. Idempotent. */
    fun clear(providerId: String)

    /** Convenience: whether a non-blank secret is currently stored. */
    fun hasSecret(providerId: String): Boolean = !secretFor(providerId).isNullOrBlank()
}
