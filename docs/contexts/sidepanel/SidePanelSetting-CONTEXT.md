# Side Panel — Setting Scope

| | |
| --- | --- |
| **Context** | Side panel → setting scope |
| **Code** | `sidepanel/` (`SidePanelSetting…` symbols) |
| **Parent** | [SidePanel context](./SidePanel-CONTEXT.md) |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

The setting scope is the app-preferences surface inside the side panel. It migrates from the former standalone `settings/` feature; in the current Android implementation, `SettingsComponent` + `SettingsScreen` still live in `settings/` and will be migrated to `SidePanelSettingComponent` + `SidePanelSettingScreen` in the `sidepanel/` module.

## Language

- **Setting** — an adjustable app-wide preference (theme, provider, credential entry points).
- **Setting surface** — the rendered preferences view inside the side panel.

## Architecture

- State will live in the setting scope's component (`SidePanelSettingComponent`); intents are its methods.
- Preference reads/writes go through `shared/externals/` interfaces (`MutableCredentialStore`, `ProviderPreferenceStore`) and the shared theme preference — never direct persistence from views.
- The setting view will render the setting surface from the component state.

## Naming convention

All setting-scope symbols and files use the `SidePanelSetting` prefix, then a role suffix per the [file-naming rules](../../architecture/modules.md) — e.g. `SidePanelSettingComponent` (component).

## Migration note

This scope supersedes the former standalone `settings/` feature. `SidePanelSettingComponent` has been created as the target; `SettingsComponent` / `SettingsView` continue to exist in `settings/` during the transition.

## Boundaries

- Owns app preferences presentation only. Secure credential storage and provider preference persistence stay in `shared/externals/`.
- Reuse theme and UI primitives from `shared/ui` and `ui/theme`.
