# Onboarding Context

| | |
|---|---|
| **Context** | Onboarding feature - first-run experience |
| **Code** | `onboarding/` |
| **Parent** | App shell |
| **Children** | None |

The Onboarding feature is the first-run product tour. It shows interactive visual demos, persists a completion flag, and then hands control to Home. Provider/model/API-key setup happens later in Settings/Home, not inside onboarding.

## Language

- **OnboardingScreen**: Root composable for the onboarding feature
- **OnboardingComponent**: Decompose component managing page navigation and completion
- **OnboardingState**: State container for current page index, completion, and demo state
- **Pages**: `EncryptedPairing`, `IdeaStudio`, `PromptQueue`, `ReasoningControl`, `WorkspaceReady`
- **Repository**: Persistence layer for the onboarding completion flag

## Architecture

### State Management

`OnboardingComponent` owns `MutableValue<OnboardingState>`:

```kotlin
data class OnboardingState(
    val currentPage: Int = 0,
    val isFinished: Boolean = false,
    val demoState: DemoState = DemoState()
) {
    val pages: List<OnboardingPage> = OnboardingPage.all
    val totalPages: Int get() = pages.size
    val isLastPage: Boolean get() = currentPage >= totalPages - 1
}

enum class OnboardingPageType {
    EncryptedPairing,
    IdeaStudio,
    PromptQueue,
    ReasoningControl,
    WorkspaceReady
}
```

### Repository Pattern

Onboarding uses a repository interface for persistence:

```kotlin
// onboarding/infrastructure/
interface OnboardingRepository {
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun completeOnboarding()
}

class DataStoreOnboardingRepository(
    private val context: Context
) : OnboardingRepository
```

### Flow Structure

Onboarding is a linear product-tour flow with back navigation and optional skip-to-last-page behavior:

```
EncryptedPairing → IdeaStudio → PromptQueue → ReasoningControl → WorkspaceReady
```

Progression does not validate credentials. `onFinishTapped()` persists completion and triggers the app shell's `onComplete` callback.

## Dependencies

- **Upstream**: None beyond Android/Compose/DataStore support
- **Downstream**: None (leaf feature)
- **Domain**: `OnboardingPage`, `OnboardingPageType`, demo value models
- **Infrastructure**: `OnboardingRepository`, `DataStoreOnboardingRepository`

## Constraints

- Onboarding must complete before Home is accessible.
- Onboarding must not store provider credentials or model preferences.
- Demo state is local UI state; only completion is persisted.

## Key Decisions

- **DataStore over SharedPreferences**: Modern async API, type-safe.
- **Repository abstraction**: Swap persistence without changing component.
- **Visual tour only**: Provider setup remains in Settings/Home so onboarding stays lightweight.
