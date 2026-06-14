# Domain Docs

How the engineering skills should consume this repo's domain documentation when exploring the codebase.

## Layout: multi-context

OpenZone uses a **multi-context** layout. Start at the repo root:

1. Read **`CONTEXT-MAP.md`** to see which contexts exist and where their glossaries live.
2. Open the relevant glossary under **`docs/contexts/`** for the area you are changing (e.g. `docs/contexts/chat/Chat-CONTEXT.md`).
3. Read **`docs/adr/`** for system-wide architectural decisions. For context-specific ADRs, check `<feature-path>/docs/adr/` when that directory exists.
4. Read **`docs/architecture/modules.md`** — read this before changing feature boundaries, shared UI/theme code, or Decompose component structure.
5. Read **`docs/architecture/coroutine-concurrency.md`** — read this before changing Kotlin code that touches concurrency, dispatcher choice, flows, or persistence.

If any of these files don't exist, **proceed silently**. Don't flag their absence; don't suggest creating them upfront. Glossaries and ADRs are added lazily when terms or decisions actually get resolved.

## Expected file structure

```text
/
├── CONTEXT-MAP.md                              # Index of contexts → glossary paths
├── docs/
│   ├── adr/                                    # System-wide ADRs
│   ├── architecture/
│   │   ├── modules.md                          # Module & package layout rules
│   │   └── coroutine-concurrency.md            # Kotlin coroutine & dispatcher rules
│   ├── agents/
│   │   ├── issue-tracker.md                    # gh CLI conventions
│   │   ├── triage-labels.md                    # Triage label vocabulary
│   │   └── domain.md                           # This file
│   └── contexts/
│       ├── app/App-CONTEXT.md
│       ├── home/Home-CONTEXT.md
│       ├── chat/Chat-CONTEXT.md
│       ├── onboarding/Onboarding-CONTEXT.md
│       ├── sidepanel/
│       │   ├── SidePanel-CONTEXT.md
│       │   ├── SidePanelSession-CONTEXT.md
│       │   └── SidePanelSetting-CONTEXT.md
│       ├── externals/Externals-CONTEXT.md
│       └── shared/Shared-CONTEXT.md
└── app/src/main/java/io/github/bengidev/openzone/
    ├── MainActivity.kt
    ├── onboarding/
    ├── home/
    ├── chat/
    ├── sidepanel/
    ├── shared/
    └── ui/theme/
```

## Use the glossary's vocabulary

When your output names a domain concept (in an issue title, a refactor proposal, a hypothesis, a test name), use the term as defined in the relevant `docs/contexts/` glossary. Don't drift to synonyms the glossary explicitly avoids.

If the concept you need isn't in the glossary yet, that's a signal — either you're inventing language the project doesn't use (reconsider) or there's a real gap (note it for `/grill-with-docs`).

## Flag ADR conflicts

If your output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> *Contradicts ADR-0001 (shared cross-cutting infra) — worth reopening because…*
