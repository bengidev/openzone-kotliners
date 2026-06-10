# Context map

Multi-context domain documentation for OpenZone. Before changing an area, read its `CONTEXT.md` (when present) for glossary terms and local conventions.

| Context | Glossary | Scope |
| ------- | -------- | ----- |
| **App** | [`docs/contexts/app/App-CONTEXT.md`](docs/contexts/app/App-CONTEXT.md) | App module shell, `MainActivity`, app-wide Compose theme, navigation |
| **Externals** | [`docs/contexts/externals/Externals-CONTEXT.md`](docs/contexts/externals/Externals-CONTEXT.md) | Feature-neutral adapters: networking (`ChatProvider`, `AuthScheme`, `SseLineDecoder`, `ChatModel`, `ModelCatalogStore`), security (`CredentialStore`, `MutableCredentialStore`), preference (`ComposerReasoningLevel`, `ProviderPreference`, `ProviderPreferenceStore`) |
| **Shared** | [`docs/contexts/shared/Shared-CONTEXT.md`](docs/contexts/shared/Shared-CONTEXT.md) | App-wide UI primitives (button styles, badges, patterns) in `shared/ui/`; palette & typography in `ui/theme/` |
| **Onboarding** | [`docs/contexts/onboarding/Onboarding-CONTEXT.md`](docs/contexts/onboarding/Onboarding-CONTEXT.md) | Onboarding pages, flow state, DataStore repository, presenter visuals |
| **Home** | [`docs/contexts/home/Home-CONTEXT.md`](docs/contexts/home/Home-CONTEXT.md) | Welcome shell, composer, model popup, particle orb; Decompose `HomeComponent`; parent of `ChatComponent` and `SidePanelSessionComponent` |
| **Chat** | [`docs/contexts/chat/Chat-CONTEXT.md`](docs/contexts/chat/Chat-CONTEXT.md) | Streaming chat thread, reasoning cards, history persistence; mirrors iOS `ChatFeature`; child of `HomeComponent` |
| **SidePanel** | [`docs/contexts/sidepanel/SidePanel-CONTEXT.md`](docs/contexts/sidepanel/SidePanel-CONTEXT.md) | Host module for session + setting sub-scopes |
| ↳ **SidePanel · session** | [`docs/contexts/sidepanel/SidePanelSession-CONTEXT.md`](docs/contexts/sidepanel/SidePanelSession-CONTEXT.md) | Saved-conversation browser (`SidePanelSessionComponent`, `SidePanelSessionSidebarView`, `SidePanelSessionSection`); supersedes old `HomeSidebarView` |
| ↳ **SidePanel · setting** | [`docs/contexts/sidepanel/SidePanelSetting-CONTEXT.md`](docs/contexts/sidepanel/SidePanelSetting-CONTEXT.md) | App preferences surface (`SidePanelSettingComponent`); supersedes old `SettingsComponent` |
| **Settings** (deprecated) | — | Legacy `settings/` package — being migrated to `SidePanelSetting*` in `sidepanel/`; see [SidePanelSetting context](docs/contexts/sidepanel/SidePanelSetting-CONTEXT.md) |
| **Theme** | [`app/src/main/java/io/github/bengidev/openzone/ui/theme/Palette.kt`](app/src/main/java/io/github/bengidev/openzone/ui/theme/Palette.kt) | Authoritative `OpenZonePalette` (iOS-faithful graphite monochrome) used throughout the app |

**System-wide ADRs:** [`docs/adr/`](docs/adr/)
**Architecture layout:** [`docs/architecture/modules.md`](docs/architecture/modules.md)

Glossary files are added lazily as terminology stabilizes. Agents should not require them to exist before starting work.
