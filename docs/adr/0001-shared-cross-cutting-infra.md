# 0001 — Shared cross-cutting infrastructure behind abstractions

## Status

Accepted

## Context

OpenZone is organized into feature contexts (onboarding, home, chat) following a
domain / application / infrastructure / presenter layering. Real provider
integration (issue #1, #2) introduces concerns that are inherently
cross-cutting and not owned by any single feature:

- **Networking** — an OpenAI-compatible streaming HTTP client and a generic
  Server-Sent Events (SSE) decoder.
- **Provider descriptors** — pure-data definitions (id, base URL, auth scheme,
  default headers) describing a chat provider such as OpenRouter.
- **Credential access** — a store interface read at call time so secrets are
  never captured at construction.

Forcing these into `chat/infrastructure/` would couple them to the chat feature
even though future features (settings, history sync) need the same primitives.
The iOS project made the equivalent allowance in its shared context.

## Decision

Introduce a feature-neutral `io.github.bengidev.openzone.shared.networking`
package for cross-cutting infrastructure that more than one feature can depend
on. Code placed here:

- MUST be feature-neutral — no imports from `chat/`, `home/`, `onboarding/`.
- MUST stay free of Android UI / Compose types (plain Kotlin + coroutines +
  OkHttp + kotlinx-serialization only).
- Exposes abstractions (interfaces, pure-data descriptors) that features wire
  together via constructor injection in `MainActivity`.

The chat-specific binding — implementing `ChatAPIClient` by translating SSE
frames into `ChatStreamingEvent` — lives in `chat/infrastructure/`, depends on
the shared primitives, and keeps the existing
`ChatAPIClient.stream(request): Flow<ChatStreamingEvent>` seam unchanged.

## Consequences

- Shared infra is reusable without creating a dependency on any feature.
- The "shared infra stays feature-neutral" guardrail must be enforced in review;
  feature semantics belong in the feature package, not in `shared/`.
- No new Gradle module is introduced — `shared/` is an internal package, mirroring
  the onboarding decision in AGENTS.md.
