package io.github.bengidev.openzone.settings.infrastructure

import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreference
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreferenceStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [ProviderPreferenceStore] test double. Backed by a StateFlow so the
 * observable contract ([preferenceFlow]) can be asserted in unit tests without
 * a real DataStore. Preserves the reasoning level across provider/model writes,
 * matching the DataStore-backed implementation.
 */
class InMemoryProviderPreferenceStore(
    initial: ProviderPreference? = null
) : ProviderPreferenceStore {

    private val _flow = MutableStateFlow(initial)

    override suspend fun preference(): ProviderPreference? = _flow.value

    override val preferenceFlow: Flow<ProviderPreference?> = _flow.asStateFlow()

    override suspend fun setProvider(providerId: String) {
        val current = _flow.value
        _flow.value = ProviderPreference(
            providerId = providerId,
            modelId = current?.modelId,
            reasoningLevel = current?.reasoningLevel ?: ComposerReasoningLevel.Off
        )
    }

    override suspend fun setModel(providerId: String, modelId: String) {
        _flow.value = ProviderPreference(
            providerId = providerId,
            modelId = modelId,
            reasoningLevel = _flow.value?.reasoningLevel ?: ComposerReasoningLevel.Off
        )
    }

    override suspend fun setReasoningLevel(level: ComposerReasoningLevel) {
        val current = _flow.value
        _flow.value = ProviderPreference(
            providerId = current?.providerId ?: "openrouter",
            modelId = current?.modelId,
            reasoningLevel = level
        )
    }
}
