package io.github.bengidev.openzone.settings.infrastructure

import io.github.bengidev.openzone.shared.networking.ProviderPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Contract tests for the in-memory store doubles. These pin the behavior the
 * concrete EncryptedSharedPreferences / DataStore impls must also honor:
 * blank-clears, presence flags, and persistence of provider+model selection.
 */
class SettingsStoresTest {

    // ----- Credential store -----

    @Test
    fun `credential store returns null when empty`() {
        val store = InMemoryCredentialStore()
        assertNull(store.secretFor("openrouter"))
        assertFalse(store.hasSecret("openrouter"))
    }

    @Test
    fun `credential store persists and reports presence`() {
        val store = InMemoryCredentialStore()
        store.setSecret("openrouter", "sk-test-123")
        assertEquals("sk-test-123", store.secretFor("openrouter"))
        assertTrue(store.hasSecret("openrouter"))
    }

    @Test
    fun `credential store trims and treats blank as clear`() {
        val store = InMemoryCredentialStore()
        store.setSecret("openrouter", "  sk-spaced  ")
        assertEquals("sk-spaced", store.secretFor("openrouter"))
        store.setSecret("openrouter", "   ")
        assertNull(store.secretFor("openrouter"))
        assertFalse(store.hasSecret("openrouter"))
    }

    @Test
    fun `credential store clear removes secret`() {
        val store = InMemoryCredentialStore()
        store.setSecret("openrouter", "sk-test")
        store.clear("openrouter")
        assertNull(store.secretFor("openrouter"))
    }

    @Test
    fun `credential store isolates providers`() {
        val store = InMemoryCredentialStore()
        store.setSecret("openrouter", "sk-a")
        store.setSecret("other", "sk-b")
        assertEquals("sk-a", store.secretFor("openrouter"))
        assertEquals("sk-b", store.secretFor("other"))
    }

    // ----- Preference store -----

    @Test
    fun `preference store starts empty`() = runTest {
        val store = InMemoryProviderPreferenceStore()
        assertNull(store.preference())
        assertNull(store.preferenceFlow.first())
    }

    @Test
    fun `preference store persists provider preserving model`() = runTest {
        val store = InMemoryProviderPreferenceStore(
            ProviderPreference("openrouter", "deepseek/deepseek-r1:free")
        )
        store.setProvider("openrouter")
        assertEquals(
            ProviderPreference("openrouter", "deepseek/deepseek-r1:free"),
            store.preference()
        )
    }

    @Test
    fun `preference store persists model selection`() = runTest {
        val store = InMemoryProviderPreferenceStore()
        store.setModel("openrouter", "qwen/qwen-2.5-72b-instruct:free")
        assertEquals(
            ProviderPreference("openrouter", "qwen/qwen-2.5-72b-instruct:free"),
            store.preferenceFlow.first()
        )
    }
}
