# Home Context

| | |
|---|---|
| **Context** | Home feature - main landing screen |
| **Code** | `home/` |
| **Parent** | App shell |
| **Children** | Chat, SidePanel (Session/Setting) |

The Home feature is the main landing screen after onboarding. It manages the message composer, model selection, and child feature composition.

## Language

- **HomeScreen**: Root composable for the home feature
- **HomeComponent**: Decompose component managing home state and child features
- **HomeState**: State container for home UI
- **Composer**: Message input area with model picker and send button
- **Model picker**: Dialog for selecting AI models from available providers
- **Side panel**: Left drawer containing Session (conversations) and Setting (preferences)

## Architecture

### State Management

`HomeComponent` owns `MutableValue<HomeState>`:

```kotlin
data class HomeState(
    val composerText: String = "",
    val selectedModel: String = "gpt-4",
    val isModelPickerOpen: Boolean = false,
    val isSidePanelOpen: Boolean = false,
    val activeChild: ActiveChild = ActiveChild.Composer
)
```

### Child Feature Composition

Home acts as a parent to Chat and SidePanel:

```kotlin
class HomeComponent(
    componentContext: ComponentContext,
    private val chatComponentFactory: ChatComponent.Factory,
    private val sessionComponentFactory: SidePanelSessionComponent.Factory,
    private val settingComponentFactory: SidePanelSettingComponent.Factory
) : ComponentContext by componentContext {
    
    private val chatComponent = childStack(
        source = source.map { it.childConfig },
        serializer = ChildConfig.serializer(),
        initialStack = { listOf(ChildConfig.Chat) },
        childFactory = { config, childContext ->
            when (config) {
                is ChildConfig.Chat -> chatComponentFactory.create(...)
                is ChildConfig.Session -> sessionComponentFactory.create(...)
                is ChildConfig.Setting -> settingComponentFactory.create(...)
            }
        }
    )
}
```

### Callbacks Pattern

Child components communicate up via callbacks:

```kotlin
interface HomeComponent {
    fun onConversationSelected(conversationId: String)
    fun onNewConversationClicked()
    fun onSettingsClicked()
    fun onBackFromChat()
}
```

## Dependencies

- **Upstream**: `shared.externals` (credential store, preference store)
- **Downstream**: Chat feature, SidePanel feature
- **Domain**: Pure Kotlin models for home state

## Constraints

- Home must not import feature-specific implementations directly
- Child components are created via factories for testability
- Side panel presentation is overlay, not navigation stack

## Migration Status

- `SidePanelComponent` (new, recommended) replaces legacy `HomeSidebarComponent`
- `SidePanelSettingComponent` (planned) will replace legacy `SettingsComponent`
