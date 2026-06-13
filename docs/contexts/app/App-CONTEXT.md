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

1. **Composition root**: `MainActivity` instantiates infrastructure dependencies (Room DB, DataStore, credential store) and injects them into the root component
2. **Global routing**: Manages navigation between top-level features (Onboarding ↔ Home ↔ Chat)
3. **Theme provision**: Wraps the app in `OpenZoneTheme` for consistent styling
4. **Lifecycle management**: Uses `lifecycleScope` for app-level coroutines

## Decompose Integration

The app shell creates a `ComponentContext` and passes it to the root `HomeComponent`:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val componentContext = DefaultComponentContext(lifecycle.asDecomposeLifecycle())
        val rootComponent = HomeComponent(componentContext, /* dependencies */)
        
        setContent {
            OpenZoneTheme {
                HomeScreen(rootComponent)
            }
        }
    }
}
```

## Routing Strategy

- **Onboarding → Home**: After onboarding completes, switch to Home
- **Home → Chat**: User selects a conversation
- **Chat → Home**: User returns via back navigation
- **Any → Settings**: Overlay presentation managed by Home

## Key Decisions

- **Single Activity pattern**: All navigation handled within `MainActivity` using Compose and Decompose
- **Manual DI**: Currently using constructor injection without Hilt; promotes explicit dependency tracking
- **Theme at root**: `OpenZoneTheme` wraps entire app for consistent Material 3 styling
- **Lifecycle binding**: Uses `lifecycle.asDecomposeLifecycle()` to sync Decompose with Android lifecycle

## Constraints

- `domain/` layer must remain pure Kotlin (no Android SDK imports)
- `application/` layer uses Decompose + Coroutines (Android-aware but UI-agnostic)
- `infrastructure/` layer contains Android-specific implementations
- `presenter/` layer is Compose-only, observes component state

## Future Considerations

- May migrate to Hilt for DI as complexity grows
- Settings currently uses legacy `SettingsComponent`; target: `SidePanelSettingComponent` (see migration plan)
