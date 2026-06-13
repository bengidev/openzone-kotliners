package io.github.bengidev.openzone.shared.externals.preference

import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import kotlinx.coroutines.flow.Flow

/**
 * Persists the user's non-secret provider/model/reasoning selection across
 * launches. Feature-neutral seam in `shared/networking/`: Settings writes it,
 * Home/Chat read it to resolve which provider+model a request targets and what
 * reasoning effort to apply. No secrets here — the API key lives in
 * [MutableCredentialStore].
 *
 * Reads are exposed both as a one-shot [preference] (for call-time resolution
 * in the chat path) and as an observable [preferenceFlow] (for reactive UI).
 */
interface ProviderPreferenceStore {
    /** Current persisted selection, or `null` if the user hasn't chosen yet. */
    suspend fun preference(): ProviderPreference?

    /** Observable stream of the persisted selection; emits `null` until set. */
    val preferenceFlow: Flow<ProviderPreference?>

    /** Persists the selected provider id, preserving any existing model and reasoning level. */
    suspend fun setProvider(providerId: String)

    /** Persists the selected model id for the current provider. */
    suspend fun setModel(providerId: String, modelId: String)

    /** Persists the reasoning effort level for the current provider. */
    suspend fun setReasoningLevel(level: ComposerReasoningLevel)
}
