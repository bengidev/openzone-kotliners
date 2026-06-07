package io.github.bengidev.openzone.settings.infrastructure

import io.github.bengidev.openzone.shared.networking.ChatModel

/**
 * Seam for fetching a provider's live model catalog. Implemented by
 * [OpenRouterModelFetcher]; injected into [io.github.bengidev.openzone.settings.application.SettingsComponent]
 * so tests can supply a deterministic stub (or a failing one to exercise the
 * curated-fallback path).
 *
 * Mirrors the iOS catalog-fetch seam.
 */
interface ModelCatalogFetcher {
    /**
     * Fetches models for [providerId] synchronously. Implementations must be
     * called off the main thread. Returns `null` on failure (network, parse,
     * or unsupported provider) so the caller falls back to the curated catalog.
     */
    fun fetchSync(providerId: String): List<ChatModel>?
}
