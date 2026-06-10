# Onboarding Context

| | |
| --- | --- |
| **Context** | Onboarding feature |
| **Code** | `onboarding/` |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

The onboarding feature owns the first-run experience. It teaches the user the core OpenZone concepts, persists completion, and then lets the app route to the main Home surface.

## Language

- **Onboarding feature** — the full first-run workflow under `onboarding/`.
- **Onboarding page** — one step in the five-page flow.
- **Completion** — persisted signal that onboarding has finished; when true, the app shell shows `HomeScreen`.
- **Demo visual** — an interactive illustration inside an onboarding page.

## Architecture

- State lives in `OnboardingState` / `OnboardingComponent`.
- User intents are modeled as methods on `OnboardingComponent`.
- Flow mutations and persistence effects live in the component via `DataStoreOnboardingRepository`.
- Compose views receive the component and call intent methods; they do not own separate flow state.
- DataStore access is isolated behind `OnboardingRepository`.

## Boundaries

- Keep onboarding-specific copy, page models, and visual workflow inside this context.
- Use `shared/ui` and `ui/theme` for reusable theme and UI primitives only.
- Do not introduce a separate view model; the Decompose component is the source of truth.
