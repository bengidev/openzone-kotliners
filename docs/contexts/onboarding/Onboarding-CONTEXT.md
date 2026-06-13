# Onboarding Context

| | |
|---|---|
| **Context** | Onboarding feature - first-run experience |
| **Code** | `onboarding/` |
| **Parent** | App shell |
| **Children** | None |

The Onboarding feature guides new users through initial setup before they can access the main app.

## Language

- **OnboardingScreen**: Root composable for the onboarding feature
- **OnboardingComponent**: Decompose component managing onboarding flow
- **OnboardingState**: State container for onboarding progress
- **Pages**: Sequential setup screens (welcome, provider setup, API key, etc.)
- **Repository**: Persistence layer for onboarding flags

## Architecture

### State Management

`OnboardingComponent` owns `MutableValue<OnboardingState>`:

```kotlin
data class OnboardingState(
    val currentPage: Page,
    val providerSelection: ProviderSelection?,
    val apiKeyInput: String = "",
    val isComplete: Boolean = false
)

enum class Page {
    Welcome, ProviderSetup, ApiKeyEntry, ModelSelection, Complete
}
```

### Repository Pattern

Onboarding uses a repository interface for persistence:

```kotlin
// domain/
interface OnboardingRepository {
    suspend fun isOnboardingComplete(): Boolean
    suspend fun markOnboardingComplete()
}

// infrastructure/
class DataStoreOnboardingRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {
    override suspend fun isOnboardingComplete(): Boolean =
        dataStore.data.first()[IS_ONBOARDING_COMPLETE] ?: false
    
    override suspend fun markOnboardingComplete() {
        dataStore.edit { it[IS_ONBOARDING_COMPLETE] = true }
    }
}
```

### Flow Structure

Onboarding is a linear flow with back navigation:

```
Welcome → Provider Setup → API Key Entry → Model Selection → Complete
```

Each page validates before allowing progression.

## Dependencies

- **Upstream**: `shared.externals` (credential store, preference store)
- **Downstream**: None (leaf feature)
- **Domain**: `OnboardingRepository` interface (pure Kotlin)
- **Infrastructure**: `DataStoreOnboardingRepositoryImpl`

## Constraints

- Onboarding must complete before Home is accessible
- API key validation before progression to prevent broken state
- No caching of sensitive credentials in memory

## Key Decisions

- **DataStore over SharedPreferences**: Modern async API, type-safe
- **Repository abstraction**: Swap implementations without changing component
- **Linear flow**: Prevents user confusion, ensures setup completeness
