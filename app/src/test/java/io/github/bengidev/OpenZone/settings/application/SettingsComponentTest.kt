package io.github.bengidev.openzone.settings.application

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.settings.infrastructure.FakeModelCatalogFetcher
import io.github.bengidev.openzone.settings.infrastructure.InMemoryCredentialStore
import io.github.bengidev.openzone.settings.infrastructure.InMemoryModelCatalogStore
import io.github.bengidev.openzone.settings.infrastructure.InMemoryProviderPreferenceStore
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.shared.externals.preference.ComposerReasoningLevel
import io.github.bengidev.openzone.shared.externals.networking.CachedCatalog
import io.github.bengidev.openzone.shared.externals.networking.ChatModel
import io.github.bengidev.openzone.shared.externals.preference.ProviderPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsComponentTest {

    private fun component(
        credentialStore: InMemoryCredentialStore = InMemoryCredentialStore(),
        preferenceStore: InMemoryProviderPreferenceStore = InMemoryProviderPreferenceStore(),
        catalogStore: InMemoryModelCatalogStore = InMemoryModelCatalogStore(),
        catalogFetcher: FakeModelCatalogFetcher? = null,
        scope: CoroutineScope,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher = UnconfinedTestDispatcher()
    ): SettingsComponent {
        val lifecycle = LifecycleRegistry()
        return SettingsComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            providers = ChatProviders.all,
            credentialStore = credentialStore,
            preferenceStore = preferenceStore,
            catalogStore = catalogStore,
            catalogFetcher = catalogFetcher,
            ioDispatcher = dispatcher,
            mainScope = scope
        )
    }

    @Test
    fun `loads defaults from empty stores`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(scope = scope)

        val state = component.state.value
        assertTrue(state.isLoaded)
        assertEquals("openrouter", state.selectedProviderId)
        assertEquals(ModelCatalog.defaultModelId("openrouter"), state.selectedModelId)
        assertFalse(state.hasApiKey)
    }

    @Test
    fun `saving key sets presence flag but never exposes secret in state`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val credentials = InMemoryCredentialStore()
        val component = component(credentialStore = credentials, scope = scope)

        component.onApiKeyDraftChanged("***")
        component.onSaveApiKey()

        val state = component.state.value
        assertTrue(state.hasApiKey)
        assertEquals("", state.apiKeyDraft)
        assertFalse(state.toString().contains("***"))
        assertEquals("***", credentials.secretFor("openrouter"))
    }

    @Test
    fun `clearing key removes secret and resets presence`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val credentials = InMemoryCredentialStore()
        val component = component(credentialStore = credentials, scope = scope)

        component.onApiKeyDraftChanged("sk-secret")
        component.onSaveApiKey()
        component.onClearApiKey()

        assertFalse(component.state.value.hasApiKey)
        assertNull(credentials.secretFor("openrouter"))
    }

    @Test
    fun `model selection persists to preference store`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val prefs = InMemoryProviderPreferenceStore()
        val component = component(preferenceStore = prefs, scope = scope)

        val target = ModelCatalog.openRouterFree[1].id
        component.onModelSelected(target)

        assertEquals(target, component.state.value.selectedModelId)
        assertEquals(ProviderPreference("openrouter", target), prefs.preference())
    }

    @Test
    fun `restores persisted selection on load`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val saved = ModelCatalog.openRouterFree[2].id
        val prefs = InMemoryProviderPreferenceStore(ProviderPreference("openrouter", saved))
        val credentials = InMemoryCredentialStore().apply { setSecret("openrouter", "sk-stored") }
        val component = component(
            credentialStore = credentials,
            preferenceStore = prefs,
            scope = scope
        )

        val state = component.state.value
        assertEquals("openrouter", state.selectedProviderId)
        assertEquals(saved, state.selectedModelId)
        assertTrue(state.hasApiKey)
    }

    @Test
    fun `ignores unknown model id`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(scope = scope)
        val before = component.state.value.selectedModelId

        component.onModelSelected("nonexistent/model")

        assertEquals(before, component.state.value.selectedModelId)
    }

    @Test
    fun `live fetch updates models and caches result`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val liveModels = listOf(
            ChatModel(id = "test/model-a:free", displayName = "Model A",
                providerId = "openrouter", isFree = true),
            ChatModel(id = "test/model-b", displayName = "Model B",
                providerId = "openrouter", isFree = false)
        )
        val fetcher = FakeModelCatalogFetcher(models = liveModels, shouldSucceed = true)
        val catalogStore = InMemoryModelCatalogStore()
        val credentials = InMemoryCredentialStore().apply { setSecret("openrouter", "sk-key") }
        val component = component(
            credentialStore = credentials,
            catalogStore = catalogStore,
            catalogFetcher = fetcher,
            scope = scope
        )

        // Fetch should have fired (stale/absent cache + key present).
        assertEquals(1, fetcher.fetchCount)
        assertEquals(liveModels, component.state.value.models)
        // Cache should be populated.
        val cached = catalogStore.cachedCatalog("openrouter")
        assertEquals(liveModels, cached?.models)
    }

    @Test
    fun `skips live fetch when cache is fresh`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val now = System.currentTimeMillis()
        val cachedModels = ModelCatalog.openRouterFree
        val freshCache = InMemoryModelCatalogStore(
            mapOf("openrouter" to CachedCatalog(models = cachedModels, fetchedAtEpochMs = now))
        )
        val fetcher = FakeModelCatalogFetcher(shouldSucceed = true)
        val credentials = InMemoryCredentialStore().apply { setSecret("openrouter", "sk-key") }

        component(
            credentialStore = credentials,
            catalogStore = freshCache,
            catalogFetcher = fetcher,
            scope = scope
        )

        // Cache is fresh — no network call.
        assertEquals(0, fetcher.fetchCount)
    }

    @Test
    fun `falls back to curated list when fetch fails`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val fetcher = FakeModelCatalogFetcher(shouldSucceed = false)
        val credentials = InMemoryCredentialStore().apply { setSecret("openrouter", "sk-key") }
        val component = component(
            credentialStore = credentials,
            catalogFetcher = fetcher,
            scope = scope
        )

        // Fetch was attempted but failed; curated fallback is shown.
        assertEquals(1, fetcher.fetchCount)
        assertEquals(ModelCatalog.openRouterFree, component.state.value.models)
    }

    @Test
    fun `reasoning level defaults to Off on empty stores`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val component = component(scope = scope)

        assertEquals(ComposerReasoningLevel.Off, component.state.value.reasoningLevel)
    }

    @Test
    fun `selecting reasoning level persists to preference store`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val prefs = InMemoryProviderPreferenceStore()
        val component = component(preferenceStore = prefs, scope = scope)

        component.onReasoningLevelSelected(ComposerReasoningLevel.Medium)

        assertEquals(ComposerReasoningLevel.Medium, component.state.value.reasoningLevel)
        assertEquals(ComposerReasoningLevel.Medium, prefs.preference()?.reasoningLevel)
    }

    @Test
    fun `restores persisted reasoning level on load`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val saved = ModelCatalog.openRouterFree[2].id
        val prefs = InMemoryProviderPreferenceStore(
            ProviderPreference("openrouter", saved, ComposerReasoningLevel.High)
        )
        val component = component(preferenceStore = prefs, scope = scope)

        assertEquals(ComposerReasoningLevel.High, component.state.value.reasoningLevel)
    }

    @Test
    fun `reasoning section visible only for reasoning-capable selected model`() = runTest {
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        // Nemotron (index 2) supports reasoning; Llama (index 0) does not.
        val reasoningModel = ModelCatalog.openRouterFree[2].id
        val plainModel = ModelCatalog.openRouterFree[0].id
        val component = component(scope = scope)

        component.onModelSelected(plainModel)
        assertFalse(component.state.value.selectedModelSupportsReasoning)

        component.onModelSelected(reasoningModel)
        assertTrue(component.state.value.selectedModelSupportsReasoning)
    }
}
