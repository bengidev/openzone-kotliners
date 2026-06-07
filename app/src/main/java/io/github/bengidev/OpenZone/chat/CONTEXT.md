# Chat context

Streaming chat thread feature. Mirrors iOS `Features/Chat` (TCA reducer `ChatFeature`). On Android, the reducer-style state holder is a `ChatComponent` (mirrors the `OnboardingComponent` pattern) and is owned by `HomeComponent` as a child feature — same composition as iOS `HomeFeature` scoping `ChatFeature`.

## Package layout

```
io.github.bengidev.openzone.chat/
├── domain/          # Models only — no Android, no Compose
├── application/     # ChatState + ChatComponent (reducer)
├── infrastructure/  # ChatAPIClient + OpenAiCompatibleStreamingClient (live) + SSE/wire models
├── presenter/       # Compose UI: ChatThreadView, ChatMessageRowView, ChatReasoningCardView
└── theme/           # ChatPalette + ChatTypography + OpenZoneChatTheme
```

## Glossary

- **Reasoning** — model chain-of-thought, streamed separately from the answer and rendered in a collapsible monospaced card. The Android reducer merges late `ThinkingDelta` events into the same reasoning row by stable `id` (iOS uses the same ID-based merge via TCA scoping).
- **Turn** — one user message + assistant reasoning + assistant answer, identified by stable message IDs and appended to the thread as a single unit.
- **Streaming status** — `IDLE` → `RUNNING` → `DONE` / `FAILED`. Drives the auto-scroll and "Streaming…" affordance.
- **Unified palette** — both Home and Chat pull from the authoritative `OpenZonePalette` (`io.github.bengidev.openzone.ui.theme.Palette`). iOS-faithful graphite monochrome (`#2B2B2B` accent, `#141414` strong control, `#F7F7F7`/`#0B0B0B` base). The previous Android-only blue accent has been removed; `HomePalette` is now a `typealias` of `OpenZonePalette` for backward source compatibility, and `ChatPalette` is derived from `OpenZonePalette` (no duplicate constants).

## Conventions

- `ChatComponent` exposes a `StateFlow<ChatState>` and `send`-style intent methods (`onDraftChanged`, `onSendTapped`, `onStopTapped`, `onClearThread`, `onToggleReasoning`).
- Streams are flow-based and consumed in a `CoroutineScope` owned by `HomeComponent`.
- Chat reuses the authoritative **`OpenZonePalette`** (`ui.theme`) for all surface/text/accent tokens. `ChatPalette` is a thin semantic wrapper (`userBubble` / `reasoningCard` / etc.) built from `OpenZonePalette` via `ChatPaletteDefaults.fromPalette(...)` — no duplicate color constants.
- iOS `ChatMessage` enum → Kotlin `sealed interface ChatMessage` with `Text` / `Thinking` / `System` variants.
- iOS `ChatMessagePayload` → split into `ChatTextMessage`, `ChatThinkingMessage`, `ChatSystemMessage`, `ChatMessageRole`, `ChatStreamingStatus` (Kotlin data classes).

## Wiring

`MainActivity` → `HomeComponent` → `chatComponent: ChatComponent` → `ChatThreadView` (when `chatState.hasMessages` is true) or `HomeWelcomeView` (when empty). The composer is shared; the `HomeComponent.onSendTapped` forwards the draft to `chatComponent.onSendTapped`.

## iOS parity

Mirrors `/Users/beng/Documents/iOS Projects/OpenZone/OpenZone/Features/Chat` (15 files). Parity audit done — all iOS source files have a Kotlin counterpart. The only structural deviation is that the Android `ChatComponent` is owned by `HomeComponent` rather than being a top-level Decompose child, because `MainActivity` doesn't yet have a multi-child nav stack (single `if/else` switch between onboarding and home).
