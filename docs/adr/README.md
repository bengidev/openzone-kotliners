# Architecture Decision Records (ADRs)

System-wide architectural decisions for OpenZone live here.

## Format

Use numbered markdown files:

```text
docs/adr/
├── 0001-example-decision.md
└── 0002-another-decision.md
```

Each ADR should include **context**, **decision**, and **consequences**. Context-specific ADRs may live under a feature path (e.g. `app/.../onboarding/docs/adr/`) when the decision only affects that area.

## When to add an ADR

Add an ADR when a decision is stable enough that agents and contributors should not rediscover it from code alone — persistence choices, module boundaries, AI provider integration patterns, and similar cross-cutting concerns.
