package io.github.bengidev.openzone.shared.networking

/**
 * A point-in-time snapshot of a provider's model catalog, plus when it was
 * fetched. Persisted by [ModelCatalogStore] so the live catalog survives
 * launches and a staleness check can decide whether to refetch.
 *
 * @property models the cached models, in provider order.
 * @property fetchedAtEpochMs wall-clock fetch time (`System.currentTimeMillis()`).
 */
data class CachedCatalog(
    val models: List<ChatModel>,
    val fetchedAtEpochMs: Long
) {
    /** Whether this snapshot is older than [ttlMs] relative to [nowEpochMs]. */
    fun isStale(nowEpochMs: Long, ttlMs: Long): Boolean =
        nowEpochMs - fetchedAtEpochMs >= ttlMs
}

/**
 * Persists a provider's live model catalog with a fetch timestamp, so the
 * composer and Settings can offer the live list across launches and refresh it
 * only when stale. Feature-neutral seam in `shared/networking/` alongside
 * [ProviderPreferenceStore]. No secrets here.
 *
 * Mirrors the iOS cached-catalog store.
 */
interface ModelCatalogStore {
    /** Cached catalog for [providerId], or `null` if nothing has been cached. */
    suspend fun cachedCatalog(providerId: String): CachedCatalog?

    /** Persists [models] for [providerId] stamped at [fetchedAtEpochMs]. */
    suspend fun saveCatalog(providerId: String, models: List<ChatModel>, fetchedAtEpochMs: Long)
}
