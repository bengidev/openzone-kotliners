# Domain Docs

How engineering skills and agents should consume domain documentation in this repo.

## Layout: multi-context

OpenZone uses a **multi-context** layout. Start at the repo root:

1. Read **`CONTEXT-MAP.md`** to see which contexts exist and where their glossaries live.
2. Open the **`CONTEXT.md`** for the area you are changing.
3. Read **`docs/adr/`** for system-wide architectural decisions.
4. Read **`docs/architecture/modules.md`** — read this before changing feature boundaries, shared UI/theme code, or Decompose component structure.
5. Read **`docs/architecture/coroutine-concurrency.md`** — read this before changing Kotlin code that touches concurrency, dispatcher choice, flows, or persistence.

If any of these files don't exist, **proceed silently**. Don't flag their absence; don't suggest creating them upfront. Glossaries and ADRs are added lazily when terms or decisions actually get resolved.

## Expected file structure

```text
/
├── CONTEXT-MAP.md                              # Index of contexts → CONTEXT.md paths
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

When output names a domain concept (issue title, refactor proposal, test name), use the term as defined in the relevant `CONTEXT.md`. Do not drift to synonyms the glossary explicitly avoids.

If the concept is not in the glossary, either reconsider the naming or note the gap for a future glossary update.

## Flag ADR conflicts

If output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> *Contradicts ADR-0001 (shared cross-cutting infra) — worth reopening because…*
