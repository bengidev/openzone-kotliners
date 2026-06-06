# Onboarding context

Pre-auth onboarding flow. Mirrors iOS `Features/Onboarding` (TCA reducer `OnboardingFeature`).

## Package layout

```
io.github.bengidev.openzone.onboarding/
├── domain/         # OnboardingPage, OnboardingPageType, OnboardingPromptOption, OnboardingFeatureHighlight, OnboardingQueueItem
├── application/    # OnboardingState + OnboardingComponent (state holder)
├── infrastructure/ # OnboardingRepository interface + DataStoreOnboardingRepository
├── presenter/      # Compose UI: OnboardingScreen, FeaturePageView, PageVisualFactory, components/, visuals/
└── theme/          # OnboardingPalette (typealias of OpenZonePalette) + tokens
```

## Glossary

- **Page type** — one of `Intro`, `Feature`, `PromptOption`, `ReasoningLevel`, `Finalize`. Drives which `PageVisualFactory` builder runs.
- **Highlight** — feature pill rendered in the bottom of feature pages (`OnboardingFeatureHighlight`).
- **Visual** — animated decorative graphic per page type (`EncryptedPairingVisualView`, `IdeaStudioVisualView`, `PromptQueueVisualView`, `ReasoningControlVisualView`, `WorkspaceReadyVisualView`).

## Conventions

- `OnboardingComponent` is a Decompose `ComponentContext`; persistence goes through `OnboardingRepository` (DataStore in production).
- `OnboardingPalette` is a `typealias` of `OpenZonePalette` (iOS-faithful graphite monochrome). Onboarding no longer uses a blue accent — the iOS-faithful palette is the single source of truth across Onboarding, Home, and Chat.
- Visual elements that previously relied on `galaxyAura` (blue-tinted overlay) have been removed; the migration to graphite surfaces is the only intended visual change.

## Wiring

`MainActivity` → `OnboardingComponent(repository = DataStoreOnboardingRepository(context))` → `OnboardingScreen` (until `onComplete` fires) → `HomeScreen` (after).

## iOS parity

Mirrors `/Users/beng/Documents/iOS Projects/OpenZone/OpenZone/Features/Onboarding`. Parity audit complete — all iOS source files have a Kotlin counterpart. The palette was historically blue-accent on Android; this has been unified to the iOS-faithful graphite `OpenZonePalette` for cross-feature consistency.
