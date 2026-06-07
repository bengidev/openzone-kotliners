package io.github.bengidev.openzone.settings.application

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.bengidev.openzone.settings.domain.ModelCatalog
import io.github.bengidev.openzone.settings.infrastructure.InMemoryCredentialStore
import io.github.bengidev.openzone.settings.infrastructure.InMemoryProviderPreferenceStore
import io.github.bengidev.openzone.chat.infrastructure.ChatProviders
import io.github.bengidev.openzone.shared.networking.ProviderPreference
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
        scope: CoroutineScope
    ): SettingsComponent {
        val lifecycle = LifecycleRegistry()
        return SettingsComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            providers = ChatProviders.all,
            credentialStore = credentialStore,
            preferenceStore = preferenceStore,
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

        component.onApiKeyDraftChanged("sk-secret-xyz")
        component.onSaveApiKey()

        val state = component.state.value
        assertTrue(state.hasApiKey)
        // Draft cleared; secret value must not linger anywhere in state.
        assertEquals("", state.apiKeyDraft)
        assertFalse(state.toString().contains("sk-secret-xyz"))
        // Secret is persisted in the store, not state.
        assertEquals("sk-secret-xyz", credentials.secretFor("openrouter"))
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
}
