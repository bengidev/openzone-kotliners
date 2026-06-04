# Domain Docs

How engineering skills and agents should consume domain documentation in this repo.

## Layout: multi-context

OpenZone uses a **multi-context** layout. Start at the repo root:

1. Read **`CONTEXT-MAP.md`** to see which contexts exist and where their glossaries live.
2. Open the **`CONTEXT.md`** for the area you are changing.
3. Read **`docs/adr/`** for system-wide architectural decisions. For context-specific ADRs, check `<context-path>/docs/adr/` when that directory exists.

If any of these files or directories are missing, **proceed silently**. Do not block work or insist on creating them up front. Add glossary entries and ADRs when terms or decisions actually stabilize (e.g. via `/real-engineer-grill-with-docs`).

## Expected file structure

```text
/
├── CONTEXT-MAP.md                       # Index of contexts → CONTEXT.md paths
├── docs/adr/                            # System-wide ADRs
└── app/
    ├── CONTEXT.md                       # App module: shell, navigation, shared theme
    └── src/main/java/io/github/bengidev/openzone/
        └── onboarding/
            ├── CONTEXT.md               # Onboarding domain language (when added)
            └── docs/adr/                # Onboarding-specific ADRs (when added)
```

## Use the glossary's vocabulary

When output names a domain concept (issue title, refactor proposal, test name), use the term as defined in the relevant `CONTEXT.md`. Do not drift to synonyms the glossary explicitly avoids.

If the concept is not in the glossary, either reconsider the naming or note the gap for a future glossary update.

## Flag ADR conflicts

If output contradicts an existing ADR, surface it explicitly rather than silently overriding:

> _Contradicts ADR-0007 (example title) — worth reopening because…_
