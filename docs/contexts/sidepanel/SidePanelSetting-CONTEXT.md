# SidePanel Setting Context

| | |
|---|---|
| **Context** | SidePanel Setting - app preferences and configuration |
| **Code** | `sidepanel/application/SidePanelSettingComponent.kt` |
| **Parent** | SidePanel |
| **Children** | None |

The SidePanel Setting scope manages app-wide preferences and settings.

**Migration status**: In progress. Legacy `SettingsComponent` currently active in `settings/` package. Target: `SidePanelSettingComponent` in `sidepanel/setting/` subdirectory.

## Language

- **SidePanelSettingComponent**: Decompose component for settings (target state)
- **SettingsComponent**: Legacy component in `settings/` package (currently active)
- **SettingsState**: State container for app preferences
- **Preference store**: DataStore-backed persistence for user preferences
- **Credential store**: Encrypted storage for API keys

## Architecture

### State Management

`SidePanelSettingComponent` (target) will own `MutableValue<SettingsState>`:

```kotlin
data class SettingsState(
    val selectedProvider: String = "openrouter",
    val apiKey: String = "",
    val defaultModel: String = "gpt-4",
    val reasoningLevel: ReasoningLevel = ReasoningLevel.Medium,
    val isApiKeyValid: Boolean = false
)

enum class ReasoningLevel {
    Low, Medium, High
}
```

### Dependency Injection

Settings depend on multiple externals:

```kotlin
class SidePanelSettingComponent(
    context: ComponentContext,
    private val credentialStore: CredentialStore,
    private val preferenceStore: PreferenceStore,
    private val modelRepository: ModelRepository
) : ComponentContext by context {
    
    fun saveApiKey(apiKey: String) {
        componentScope.launch {
            credentialStore.storeApiKey(apiKey)
            state.value = state.value.copy(
                apiKey = apiKey,
                isApiKeyValid = credentialStore.validateApiKey(apiKey)
            )
        }
    }
    
    fun updatePreferences(preferences: UserPreferences) {
        componentScope.launch {
            preferenceStore.savePreferences(preferences)
            state.value = state.value.copy(
                selectedProvider = preferences.provider,
                defaultModel = preferences.model,
                reasoningLevel = preferences.reasoningLevel
            )
        }
    }
}
```

### UI Structure

Settings UI is a form-based layout with sections:

- **Provider Configuration**: API key entry, provider selection
- **Model Preferences**: Default model, reasoning level
- **Account**: API key validation status, logout

## Dependencies

- **Upstream**: `shared.externals` (credential store, preference store, model repository)
- **Downstream**: None (leaf feature)
- **Domain**: `UserPreferences`, `ReasoningLevel` (pure Kotlin)

## Constraints

- API keys must be validated before saving
- Credentials stored via `EncryptedCredentialStore` (AES-256)
- Changes persist immediately to prevent data loss

## Migration Plan

1. Create `sidepanel/setting/` subdirectory structure
2. Implement `SidePanelSettingComponent` with same functionality as `SettingsComponent`
3. Update `HomeComponent` to inject `SidePanelSettingComponent.Factory`
4. Deprecate `settings/` package
5. Remove legacy code after verification

## Key Decisions

- **EncryptedSharedPreferences**: Industry-standard encryption for sensitive data
- **Immediate persistence**: Prevents user confusion about unsaved changes
- **Factory pattern**: Enables dependency injection and testing
