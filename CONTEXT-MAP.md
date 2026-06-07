# Context map

Multi-context domain documentation for OpenZone. Before changing an area, read its `CONTEXT.md` (when present) for glossary terms and local conventions.

| Context | Glossary | Scope |
| ------- | -------- | ----- |
| **App** | [`app/CONTEXT.md`](app/CONTEXT.md) | App module shell, `MainActivity`, app-wide Compose theme, navigation |
| **Onboarding** | [`app/src/main/java/io/github/bengidev/openzone/onboarding/CONTEXT.md`](app/src/main/java/io/github/bengidev/openzone/onboarding/CONTEXT.md) | Onboarding pages, flow state, DataStore repository, presenter visuals; `OnboardingPalette` is a typealias of `OpenZonePalette` (graphite) |
| **Home** | — | Welcome shell, composer, particle orb; Decompose `HomeComponent`; palette from iOS `OpenSpacePalette` |
| **Chat** | [`app/src/main/java/io/github/bengidev/openzone/chat/CONTEXT.md`](app/src/main/java/io/github/bengidev/openzone/chat/CONTEXT.md) | Streaming chat thread, reasoning cards, mock API client; mirrors iOS `ChatFeature`; child of `HomeComponent` |
| **Settings** | [`app/src/main/java/io/github/bengidev/openzone/settings/CONTEXT.md`](app/src/main/java/io/github/bengidev/openzone/settings/CONTEXT.md) | Secure API-key entry (EncryptedSharedPreferences) + provider/model selection (DataStore); opens from Home top bar; child of `HomeComponent`; decoupled from Chat via `shared/networking/` store interfaces |
| **Theme** | [`app/src/main/java/io/github/bengidev/openzone/ui/theme/Palette.kt`](app/src/main/java/io/github/bengidev/openzone/ui/theme/Palette.kt) | Authoritative `OpenZonePalette` (iOS-faithful graphite monochrome) used by Home + Chat |
| **Shared / networking** | — | Feature-neutral provider integration primitives (`ChatProvider`, `AuthScheme`, `CredentialStore` / `MutableCredentialStore`, `ProviderPreferenceStore`, `ProviderPreference`, `SseLineDecoder`) in `shared/networking/`; see [`docs/adr/0001-shared-cross-cutting-infra.md`](docs/adr/0001-shared-cross-cutting-infra.md). Must stay free of feature and Compose imports |

**System-wide ADRs:** [`docs/adr/`](docs/adr/)

Glossary files are added lazily as terminology stabilizes. Agents should not require them to exist before starting work.
