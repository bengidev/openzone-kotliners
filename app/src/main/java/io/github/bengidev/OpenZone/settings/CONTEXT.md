# Settings context (deprecated)

> **Deprecated:** This package is superseded by the side panel **setting scope**
> (`sidepanel/application/SidePanelSettingComponent`, `sidepanel/presenter/SidePanelSettingView`).
> Settings now opens from the session sidebar gear button or the composer API-key hint,
> mirroring iOS `SidePanelSettingFeature`. `HomeComponent` composes `SidePanelComponent`
> instead of `SettingsComponent`.

Legacy package layout (retained for tests/reference):

```
io.github.bengidev.openzone.settings/
├── domain/          # ModelCatalog curated fallback
├── application/     # SettingsState + SettingsComponent (deprecated)
├── theme/           # SettingsTheme palette wrapper
├── presenter/       # SettingsView (deprecated)
└── SettingsScreen.kt
```

See [docs/contexts/sidepanel/SidePanelSetting-CONTEXT.md](../../docs/contexts/sidepanel/SidePanelSetting-CONTEXT.md) for the current architecture.
