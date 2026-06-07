package io.github.bengidev.openzone.settings.infrastructure

import io.github.bengidev.openzone.shared.networking.CachedCatalog
import io.github.bengidev.openzone.shared.networking.ChatModel
import io.github.bengidev.openzone.shared.networking.ModelCatalogStore

/**
 * In-memory [ModelCatalogStore] test double. Stores catalogs per provider id
 * in a plain map — no DataStore, no coroutine overhead — so unit tests can
 * assert catalog caching behavior deterministically.
 */
class InMemoryModelCatalogStore(
    private val initial: Map<String, CachedCatalog> = emptyMap()
) : ModelCatalogStore {

    private val store: MutableMap<String, CachedCatalog> = initial.toMutableMap()

    override suspend fun cachedCatalog(providerId: String): CachedCatalog? =
        store[providerId]

    override suspend fun saveCatalog(
        providerId: String,
        models: List<ChatModel>,
        fetchedAtEpochMs: Long
    ) {
        store[providerId] = CachedCatalog(models = models, fetchedAtEpochMs = fetchedAtEpochMs)
    }
}
