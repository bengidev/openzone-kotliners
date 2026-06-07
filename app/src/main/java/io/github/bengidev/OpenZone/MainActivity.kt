package io.github.bengidev.openzone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.bengidev.openzone.home.HomeScreen
import io.github.bengidev.openzone.home.application.HomeComponent
import io.github.bengidev.openzone.onboarding.OnboardingScreen
import io.github.bengidev.openzone.onboarding.application.OnboardingComponent
import io.github.bengidev.openzone.onboarding.infrastructure.DataStoreOnboardingRepository
import io.github.bengidev.openzone.settings.infrastructure.DataStoreModelCatalogStore
import io.github.bengidev.openzone.settings.infrastructure.DataStoreProviderPreferenceStore
import io.github.bengidev.openzone.settings.infrastructure.EncryptedCredentialStore
import io.github.bengidev.openzone.settings.infrastructure.OpenRouterModelFetcher
import io.github.bengidev.openzone.ui.theme.AppTheme
import io.github.bengidev.openzone.ui.theme.LocalAppTheme
import io.github.bengidev.openzone.ui.theme.OpenZoneTheme

class MainActivity : ComponentActivity() {
    private val lifecycleRegistry = LifecycleRegistry()
    private var showHome by mutableStateOf(false)
    private var appTheme by mutableStateOf(AppTheme.System)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val componentContext = DefaultComponentContext(lifecycle = lifecycleRegistry)

        val onboardingComponent = OnboardingComponent(
            componentContext = componentContext,
            repository = DataStoreOnboardingRepository(this),
            onComplete = { showHome = true }
        )

        val homeComponent = HomeComponent(
            componentContext = componentContext,
            credentialStore = EncryptedCredentialStore(applicationContext),
            preferenceStore = DataStoreProviderPreferenceStore(applicationContext),
            catalogStore = DataStoreModelCatalogStore(applicationContext),
            catalogFetcher = OpenRouterModelFetcher(),
            onSidebarToggle = { /* SideStory overlay — future */ }
        )

        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkTheme = appTheme.resolveDark(systemDark)

            CompositionLocalProvider(LocalAppTheme provides appTheme) {
                OpenZoneTheme(darkTheme = darkTheme, dynamicColor = false) {
                    if (showHome) {
                        HomeScreen(component = homeComponent, darkTheme = darkTheme)
                    } else {
                        OnboardingScreen(
                            component = onboardingComponent,
                            darkTheme = darkTheme,
                            onThemeToggle = { appTheme = appTheme.next }
                        )
                    }
                }
            }
        }
    }
}
