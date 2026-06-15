package io.github.bengidev.openzone.shared.externals.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.bengidev.openzone.shared.externals.security.MutableCredentialStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * [ProviderPreferenceStore] backed by Preferences DataStore. Persists the
 * non-secret provider/model/reasoning selection across launches. Android analog
 * of the iOS UserDefaults-backed selection store. Mirrors the established
 * `DataStoreOnboardingRepository` pattern (interface in the shared seam,
 * concrete DataStore impl here).
 *
 * No secrets are stored here — the API key lives in [MutableCredentialStore].
 */
class DataStoreProviderPreferenceStore(
    private val context: Context
) : ProviderPreferenceStore {

    override suspend fun preference(): ProviderPreference? =
        context.dataStore.data.map { it.toPreference() }.first()

    override val preferenceFlow: Flow<ProviderPreference?>
        get() = context.dataStore.data.map { it.toPreference() }

    override suspend fun setProvider(providerId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PROVIDER_ID] = providerId
        }
    }

    override suspend fun setModel(providerId: String, modelId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PROVIDER_ID] = providerId
            prefs[KEY_MODEL_ID] = modelId
        }
    }

    override suspend fun setReasoningLevel(level: ComposerReasoningLevel) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REASONING_LEVEL] = level.name
        }
    }

    private fun Preferences.toPreference(): ProviderPreference? {
        val providerId = this[KEY_PROVIDER_ID] ?: return null
        return ProviderPreference(
            providerId = providerId,
            modelId = this[KEY_MODEL_ID],
            reasoningLevel = this[KEY_REASONING_LEVEL]?.let(::reasoningLevelFromName)
                ?: ComposerReasoningLevel.High
        )
    }

    /** Tolerant parse: an unknown/legacy persisted value falls back to [ComposerReasoningLevel.High]. */
    private fun reasoningLevelFromName(name: String): ComposerReasoningLevel =
        ComposerReasoningLevel.entries.firstOrNull { it.name == name } ?: ComposerReasoningLevel.High

    companion object {
        private val KEY_PROVIDER_ID = stringPreferencesKey("provider_id")
        private val KEY_MODEL_ID = stringPreferencesKey("model_id")
        private val KEY_REASONING_LEVEL = stringPreferencesKey("reasoning_level")

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "provider_prefs"
        )
    }
}
