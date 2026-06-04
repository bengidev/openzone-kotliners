package io.github.bengidev.openzone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.bengidev.openzone.onboarding.OnboardingScreen
import io.github.bengidev.openzone.onboarding.application.OnboardingComponent
import io.github.bengidev.openzone.onboarding.infrastructure.DataStoreOnboardingRepository
import io.github.bengidev.openzone.ui.theme.OpenZoneTheme

class MainActivity : ComponentActivity() {
    private val lifecycleRegistry = LifecycleRegistry()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val onboardingComponent = OnboardingComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycleRegistry),
            repository = DataStoreOnboardingRepository(this),
            onComplete = {
                finish()
            }
        )

        setContent {
            OpenZoneTheme {
                OnboardingScreen(
                    component = onboardingComponent,
                    onThemeToggle = null
                )
            }
        }
    }
}
