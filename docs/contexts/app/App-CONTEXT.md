# App Context

| | |
|---|---|
| **Context** | App shell and routing |
| **Code** | `MainActivity.kt` and root-level theme |

The app shell provides the entry point, global routing, and feature composition.

## Structure

```
io.github.bengidev.openzone/
├── MainActivity.kt              # Composition root + routing
├── ui/theme/
│   ├── Palette.kt              # iOS-faithful graphite palette
│   ├── Typography.kt           # App typography
│   └── Theme.kt                # Material theme wrapper
└── (feature packages)
```

## Responsibilities

1. **Composition root**: `MainActivity` instantiates infrastructure dependencies (Room DB, DataStore-backed stores, credential store) and injects them into feature components
2. **Global routing**: Manages first-run switch from Onboarding to Home
3. **Theme provision**: Wraps the app in `OpenZoneTheme` and provides `LocalAppTheme`
4. **Lifecycle setup**: Creates the Decompose `DefaultComponentContext` from an Essenty `LifecycleRegistry`

## Decompose Integration

The app shell creates a `ComponentContext` and passes it to `OnboardingComponent` and `HomeComponent`:

```kotlin
class MainActivity : ComponentActivity() {
    private val lifecycleRegistry = LifecycleRegistry()
    private var showHome by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val componentContext = DefaultComponentContext(lifecycle = lifecycleRegistry)
        val homeComponent = HomeComponent(componentContext, /* dependencies */)
        val onboardingComponent = OnboardingComponent(
            componentContext = componentContext,
            repository = DataStoreOnboardingRepository(this),
            onComplete = { showHome = true }
        )

        setContent {
            OpenZoneTheme {
                if (showHome) HomeScreen(homeComponent) else OnboardingScreen(onboardingComponent)
            }
        }
    }
}
```

## Routing Strategy

- **Onboarding → Home**: After onboarding completes, `showHome` switches the rendered root screen
- **Home welcome ↔ Chat thread**: `HomeScreen` swaps content based on `ChatComponent` messages
- **Home → Settings**: Settings is presented by `HomeScreen` from `HomeComponent.settingsComponent`
- **Home → Session sidebar**: Saved-conversation sidebar is presented by `HomeScreen` from `SidePanelSessionComponent`

## Key Decisions

- **Single Activity pattern**: All navigation handled within `MainActivity` using Compose and Decompose
- **Manual DI**: Currently using constructor injection without Hilt; promotes explicit dependency tracking
- **Theme at root**: `OpenZoneTheme` wraps entire app for consistent Material 3 styling
- **Lifecycle binding**: Uses an Essenty `LifecycleRegistry` with `DefaultComponentContext`

## Constraints

- `domain/` layer must remain pure Kotlin (no Android SDK imports)
- `application/` layer uses Decompose + Coroutines (Android-aware but UI-agnostic)
- `infrastructure/` layer contains Android-specific implementations
- `presenter/` layer is Compose-only, observes component state

## Future Considerations

- May migrate to Hilt for DI as complexity grows
- Settings currently uses legacy `SettingsComponent`; target: `SidePanelSettingComponent` (see migration plan).
