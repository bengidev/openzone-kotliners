# Coroutine & Concurrency Rules

Part of this repo's multi-context documentation. See [CONTEXT-MAP.md](../../CONTEXT-MAP.md) for per-feature glossaries and [docs/agents/domain.md](../agents/domain.md) for how agents consume domain docs. Architectural exceptions belong in `docs/adr/` when accepted.

OpenZone uses Kotlin coroutines with strict structured concurrency and a small set of explicit dispatcher rules. These guidelines exist to keep the app responsive, predictable, and safe to evolve — not to suppress warnings.

## Dispatchers

```text
Main              UI state, Decompose components, Compose
Default           Pure computation, mapping, decoding
IO                Network, disk I/O, Room, DataStore, EncryptedSharedPreferences
Unconfined        Never — use explicit dispatchers instead
```

- Keep feature component state updates on `Main.immediate` so Compose observers see changes immediately.
- Keep long-running or fallible work (streaming, persistence, network) in explicit `CoroutineScope` or `viewModelScope`/`componentScope`.
- Avoid `GlobalScope`. If shared state is needed, model it as a `Store` or `Client` with controlled scope.

## State management rules

- Feature state lives in `MutableValue<State>` (Decompose) or `MutableStateFlow<State>` (Compose-only).
- Feature mutations happen only through the owning component's intent methods.
- Side effects return `Flow<Effect>` or run in a scoped `coroutineScope {}` block.
- Views observe state via `subscribeAsState()` or `collectAsState()` and should not create parallel view-model state for the same screen.
- Tests should use `TestCoroutineScope` to assert state transitions and effects.

## Flow rules

- Expose read-only `Flow<T>` from stores/clients; keep the mutable source private.
- Use `StateFlow` for UI-facing state that has a current value.
- Use `SharedFlow` for one-shot events (errors, navigation, toasts).
- Always apply `flowOn(context)` when switching dispatchers inside a flow builder.
- Use `combine`, `flatMapLatest`, or `flatMapMerge` to join streams — avoid nested `collect {}` calls.

## SSE & streaming rules

The `OpenAiCompatibleStreamingClient` returns `Flow<ChatStreamingEvent>`:

```kotlin
fun stream(request: ChatRequest): Flow<ChatStreamingEvent> = flow {
    val response = httpClient.newCall(httpRequest).execute()
    response.use { resp ->
        SseLineDecoder.decode(resp.body!!).collect { line ->
            when {
                line.isEmpty() -> return@collect
                line.data == "[DONE]" -> return@collect
                else -> emit(parseChunk(line.data))
            }
        }
    }
}.flowOn(Dispatchers.IO)
```

- Use `flowOn(Dispatchers.IO)` for network-bound flows.
- Cancel streaming flows when the component is destroyed or the conversation ends.

## Persistence rules

- Keep Room and DataStore access behind infrastructure clients (`ChatHistoryStore`, `OnboardingRepository`, `ProviderPreferenceStore`).
- Do not let Compose views or Decompose components perform persistence side effects directly.
- Use `suspend` functions for single operations, `Flow` for observable queries.
- Wrap Room transactions in `withContext(Dispatchers.IO) { ... }`.

## Security rules

- Read credentials from `EncryptedCredentialStore` at call time, never at construction/injection time. This keeps key rotation, logout, and token expiry responsive.
- Never log credentials, tokens, or raw request/response bodies in production builds.

## Review checklist

Before merging Kotlin code, verify:

1. `./gradlew :app:assembleDebug` succeeds.
2. `./gradlew :app:testDebugUnitTest` succeeds when unit tests are affected.
3. No coroutine `GlobalScope` usage, `runBlocking` in production code, or unbounded `Dispatchers.Default` fan-out.
4. Any dispatcher workaround or concurrency boundary change is documented in code and, if architectural, in an ADR.
