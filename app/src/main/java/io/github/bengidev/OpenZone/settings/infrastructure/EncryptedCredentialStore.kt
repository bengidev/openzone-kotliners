package io.github.bengidev.openzone.settings.infrastructure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.github.bengidev.openzone.shared.networking.MutableCredentialStore

/**
 * [MutableCredentialStore] backed by EncryptedSharedPreferences. The API key is
 * encrypted at rest with a key held in the Android Keystore (AES-256-GCM for
 * both key and value). Android analog of the iOS Keychain-backed store.
 *
 * Per-provider secrets are stored under the key `secret_<providerId>`. Secrets
 * are never logged, never echoed, and never leave the device. A blank write is
 * normalized to a clear so an empty text field doesn't persist an empty key.
 *
 * The encrypted prefs file is opened lazily on first access so constructing the
 * store (e.g. in `MainActivity`) never does keystore I/O on the main thread
 * before it's needed.
 */
class EncryptedCredentialStore(
    private val context: Context,
    private val fileName: String = DEFAULT_FILE_NAME
) : MutableCredentialStore {

    private val prefs: SharedPreferences by lazy { openEncryptedPrefs() }

    override fun secretFor(providerId: String): String? =
        prefs.getString(keyFor(providerId), null)?.takeIf { it.isNotBlank() }

    override fun setSecret(providerId: String, secret: String) {
        val trimmed = secret.trim()
        if (trimmed.isEmpty()) {
            clear(providerId)
            return
        }
        prefs.edit().putString(keyFor(providerId), trimmed).apply()
    }

    override fun clear(providerId: String) {
        prefs.edit().remove(keyFor(providerId)).apply()
    }

    private fun openEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            fileName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun keyFor(providerId: String): String = "secret_$providerId"

    companion object {
        private const val DEFAULT_FILE_NAME = "openzone_credentials"
    }
}
