package io.github.bengidev.openzone.shared.externals.networking

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * [ModelCatalogStore] backed by Preferences DataStore. Serializes the model
 * list as JSON alongside the fetch timestamp. Uses a dedicated DataStore
 * (`catalog_prefs`) separate from provider/model selection so the two concerns
 * stay independently readable and writable.
 */
class DataStoreModelCatalogStore(
    private val context: Context
) : ModelCatalogStore {

    override suspend fun cachedCatalog(providerId: String): CachedCatalog? {
        val prefs = context.catalogDataStore.data.first()
        val ts = prefs[timestampKey(providerId)] ?: return null
        val raw = prefs[modelsKey(providerId)] ?: return null
        val models = runCatching {
            json.decodeFromString(ListSerializer(ChatModel.serializer()), raw)
        }.getOrNull() ?: return null
        return CachedCatalog(models = models, fetchedAtEpochMs = ts)
    }

    override suspend fun saveCatalog(
        providerId: String,
        models: List<ChatModel>,
        fetchedAtEpochMs: Long
    ) {
        val raw = json.encodeToString(ListSerializer(ChatModel.serializer()), models)
        context.catalogDataStore.edit { prefs ->
            prefs[modelsKey(providerId)] = raw
            prefs[timestampKey(providerId)] = fetchedAtEpochMs
        }
    }

    private fun modelsKey(providerId: String) =
        stringPreferencesKey("catalog_models_$providerId")

    private fun timestampKey(providerId: String) =
        longPreferencesKey("catalog_ts_$providerId")

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        val Context.catalogDataStore by preferencesDataStore(name = "catalog_prefs")
    }
}
