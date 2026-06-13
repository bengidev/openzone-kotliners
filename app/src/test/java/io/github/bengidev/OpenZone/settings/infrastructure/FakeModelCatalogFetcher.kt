package io.github.bengidev.openzone.settings.infrastructure

import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import io.github.bengidev.openzone.shared.externals.networking.ModelCatalogFetcher

/**
 * Deterministic [ModelCatalogFetcher] test double. Returns [models] when
 * [shouldSucceed] is true, `null` otherwise — letting tests exercise both the
 * live-fetch success path and the curated-fallback path in isolation.
 */
class FakeModelCatalogFetcher(
    private val models: List<ChatModel> = emptyList(),
    private val shouldSucceed: Boolean = true
) : ModelCatalogFetcher {
    var fetchCount: Int = 0
        private set

    override fun fetchSync(providerId: String): List<ChatModel>? {
        fetchCount++
        return if (shouldSucceed) models else null
    }
}
