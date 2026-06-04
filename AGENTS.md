# AGENTS.md

## Agent skills

Project agents should preserve the native Android direction and keep AI provider implementation separated from UI code.

### Onboarding (internal module)

Onboarding lives under `app/src/main/java/io/github/bengidev/openzone/onboarding/` as an **internal feature package**, not a standalone Gradle module (`:onboarding`).

Preserve the existing layering when changing onboarding:

- **domain** — models only; no Android or Compose imports
- **application** — `OnboardingComponent` / state; depends on `OnboardingRepository`, not DataStore
- **infrastructure** — `OnboardingRepository` interface; concrete storage (e.g. DataStore) stays here
- **presenter** — Compose UI; talks to `OnboardingComponent`, not persistence
- **theme** — onboarding design tokens (`OpenZoneOnboardingTheme`)

Do not reintroduce a separate `:onboarding` library module unless the team explicitly decides to extract and publish it.

### Issue tracker

GitHub Issues on `bengidev/openzone-kotliners`. See `docs/agents/issue-tracker.md`.

### Triage labels

Five canonical roles mapped 1:1 to label strings of the same name. See `docs/agents/triage-labels.md`.

### Domain docs

Multi-context: `CONTEXT-MAP.md` at the repo root points to per-context `CONTEXT.md` files; system-wide ADRs in `docs/adr/`. See `docs/agents/domain.md`.
