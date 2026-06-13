# SidePanel Setting Context

| | |
|---|---|
| **Context** | SidePanel Setting - app preferences and configuration |
| **Code** | `sidepanel/application/SidePanelSettingComponent.kt` |
| **Parent** | SidePanel |
| **Children** | None |

The SidePanel Setting scope manages app-wide provider, model, reasoning, and credential preferences.

**Migration status**: In progress. Legacy `SettingsComponent` currently active in `settings/` package. Target: `SidePanelSettingComponent` in `sidepanel/application/`.

## Language

- **SidePanelSettingComponent**: Decompose component for settings (target state)
- **SettingsComponent**: Legacy component in `settings/` package (currently active)
- **SettingsState**: State container for app preferences
- **Preference store**: DataStore-backed persistence for user preferences
- **Credential store**: Encrypted storage for API keys

## Architecture

### State Management

`SidePanelSettingComponent` owns `MutableValue<SidePanelSettingComponent.State>`:

```kotlin
data class State(
    val providers: List<ChatProvider> = emptyList(),
    val selectedProviderId: String? = null,
    val models: List<ChatModel> = emptyList(),
    val selectedModelId: String? = null,
    val reasoningLevel: ComposerReasoningLevel = ComposerReasoningLevel.Off,
    val apiKeyDraft: String = "",
    val hasApiKey: Boolean = false,
    val isLoaded: Boolean = false,
    val isLoadingModels: Boolean = false,
    val modelSupportsReasoning: Boolean = false
)

enum class ComposerReasoningLevel {
    Off, Low, Medium, High
}
```

### Dependency Injection

Settings depend on multiple externals:

```kotlin
class SidePanelSettingComponent(
    componentContext: ComponentContext,
    private val providers: List<ChatProvider>,
    private val credentialStore: MutableCredentialStore,
    private val preferenceStore: ProviderPreferenceStore,
    private val catalogStore: ModelCatalogStore? = null,
    private val catalogFetcher: ModelCatalogFetcher? = null
) : ComponentContext by componentContext {
    fun onSaveApiKey() {
        val draft = state.value.apiKeyDraft.trim()
        val providerId = state.value.selectedProviderId ?: return
        credentialStore.setSecret(providerId, draft)
    }

    fun onModelSelected(modelId: String) {
        val providerId = state.value.selectedProviderId ?: return
        preferenceStore.setModel(providerId, modelId)
    }

    fun onReasoningLevelSelected(level: ComposerReasoningLevel) {
        preferenceStore.setReasoningLevel(level)
    }
}
```

### UI Structure

Settings UI is a form-based layout with sections:

- **Provider Configuration**: API key draft/save/clear, provider selection
- **Model Preferences**: Model selection, reasoning level
- **Catalog**: Cached model list with optional live refresh

## Dependencies

- **Upstream**: `shared.externals` credential, preference, and model catalog stores
- **Downstream**: None (leaf feature)
- **Domain**: `ComposerReasoningLevel`, `ProviderPreference`, `ChatProvider`, `ChatModel`

## Constraints

- API keys must never be exposed through state; only `apiKeyDraft` and `hasApiKey` are surfaced.
- Credentials stored via `EncryptedCredentialStore` (EncryptedSharedPreferences)
- Changes persist immediately to prevent data loss

## Migration Plan

1. Keep `SidePanelSettingComponent` functionally aligned with legacy `SettingsComponent`.
2. Add a presenter surface for the setting scope.
3. Update `HomeComponent` to use `SidePanelSettingComponent` for settings presentation.
4. Deprecate `settings/` package.
5. Remove legacy code after verification.

## Key Decisions

- **EncryptedSharedPreferences**: Industry-standard encryption for sensitive data
- **Immediate persistence**: Prevents user confusion about unsaved changes
- **Constructor injection**: Keeps stores explicit and testable
