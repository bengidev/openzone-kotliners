# Settings context

Settings feature — secure API key entry plus provider/model selection. Internal
app package (not a separate Gradle module), mirroring the onboarding/chat
layering. Opens from the Home top bar as its own presented surface (Android
analog of the iOS presented Settings sheet).

## Package layout

```
io.github.bengidev.openzone.settings/
├── domain/          # Models only — no Android, no Compose
│                    #   CatalogModel, ModelCatalog (curated free-model fallback)
├── application/     # SettingsState + SettingsComponent (Decompose reducer)
├── infrastructure/  # EncryptedCredentialStore (EncryptedSharedPreferences)
│                    #   DataStoreProviderPreferenceStore (DataStore Preferences)
├── theme/           # SettingsPalette (typealias of OpenZonePalette) + OpenZoneSettingsTheme
├── presenter/       # SettingsView (stateless content)
└── SettingsScreen.kt# presented surface wrapper (top bar + close)
```

## Decoupling seam

Settings and Chat never import each other's presenter code. They communicate
only through feature-neutral interfaces in `shared/networking/`:

- **`CredentialStore`** (read-only) — Chat's streaming client resolves the API
  key at call time.
- **`MutableCredentialStore`** (read-write, extends `CredentialStore`) — Settings
  persists / clears the key. Concrete impl: `EncryptedCredentialStore`.
- **`ProviderPreferenceStore`** — persists the non-secret provider/model
  selection. Concrete impl: `DataStoreProviderPreferenceStore`. Settings writes
  it; Home/Chat read it to resolve which provider+model a request targets.
- **`ProviderPreference`** — pure-data selection record (no secret).

One concrete class (`EncryptedCredentialStore`) implements both the read and
write interfaces; Chat only ever sees the read-only surface.

## Security

- The API key is stored with **EncryptedSharedPreferences** (AES-256-GCM value
  encryption, AES-256-SIV key encryption; master key in the Android Keystore).
  Android analog of the iOS Keychain store.
- The secret **never enters `SettingsState`** — only a presence flag
  (`hasApiKey`) is surfaced. The text field uses `PasswordVisualTransformation`.
- The key is never logged, never echoed, and never committed. Blank writes are
  normalized to a clear.

## Glossary

- **Curated fallback catalog** — a small, stable list of free-tier models
  (`ModelCatalog.openRouterFree`) shown when a live model list is unavailable.
  A live catalog fetch can layer on top later without changing the store seam.
- **Provider preference** — the persisted `{providerId, modelId}` selection,
  restored on launch so the choice survives app restarts.

## Conventions

- `SettingsComponent` follows the `OnboardingComponent` pattern: `MutableValue`
  state, intent methods, a Main-immediate scope (injectable for tests).
- All colors come from the authoritative `OpenZonePalette` via
  `SettingsTheme.palette`; no hardcoded `Color(0xFF…)` tokens.
- In-memory fakes (`InMemoryCredentialStore`, `InMemoryProviderPreferenceStore`)
  live in the test sourceset and back the store contract tests.

## Wiring

`MainActivity` constructs `EncryptedCredentialStore` + `DataStoreProviderPreferenceStore`
and injects them into `HomeComponent`, which owns a child `SettingsComponent`
(same composition pattern as it owns `ChatComponent`). `HomeScreen` overlays
`SettingsScreen` when `HomeState.isSettingsPresented` is true; the top-bar
settings action toggles it.

## iOS parity

Mirrors the iOS Settings surface: API-key entry (Keychain → EncryptedSharedPreferences),
provider selection (OpenRouter-first), and curated free-model fallback. Selection
persists across launches (UserDefaults → DataStore Preferences).
