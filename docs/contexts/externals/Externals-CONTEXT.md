# Externals Context

| | |
| --- | --- |
| **Context** | External integrations |
| **Code** | `shared/externals/` |
| **Map** | [CONTEXT-MAP.md](../../../CONTEXT-MAP.md) |
| **Layout rules** | [docs/architecture/modules.md](../../architecture/modules.md) |

`shared/externals/` holds feature-neutral adapters for systems outside the app: networking primitives, persisted preferences, and secure credential storage. Chat domain types and feature-specific clients live in feature packages, not here.

```text
shared/externals/
├── networking/     # ChatProvider, AuthScheme, SseLineDecoder, ChatModel, ModelCatalogStore, DataStoreModelCatalogStore, OpenRouterModelFetcher
├── preference/     # ComposerReasoningLevel, ProviderPreference, ProviderPreferenceStore, DataStoreProviderPreferenceStore
└── security/       # CredentialStore, MutableCredentialStore, EncryptedCredentialStore
```

## Language

- **AI provider** — a `ChatProvider` descriptor (endpoint, auth scheme, default headers).
- **Provider preference** — persisted provider id, model id, and reasoning tier (`ProviderPreference`).
- **Reasoning level** — the closed `ComposerReasoningLevel` enum mapped to `reasoning.effort` on the wire.
- **Credential store** — secure storage for the provider API secret (`CredentialStore` / `MutableCredentialStore`).
- **Model catalog store** — the cached live list of models from a provider (`ModelCatalogStore`), with `DataStoreModelCatalogStore` as the persistent implementation.

## Built-in AI providers

Shipped backends are values in `ChatProviders` in `chat/infrastructure/`. Currently only one provider is built-in:

| ID | Display name | Base URL |
| --- | --- | --- |
| `openrouter` | OpenRouter | `https://openrouter.ai/api/v1` |

Chat domain types (e.g., `ChatConversation`) belong in `chat/domain/` rather than here. Provider orchestration clients live in `chat/infrastructure/` or `shared/externals/` when truly cross-cutting. This mirrors the iOS layout where feature clients stay feature-scoped.

## Architecture

- Externals code must not import or reference feature UI or component code.
- Feature domain types belong in feature packages.
- Feature orchestration clients belong in the owning feature's package.
- Same app target today — package boundaries are the contract until promoted to a library module.

## Dependency injection

Externals exposes interfaces (`CredentialStore`, `ProviderPreferenceStore`, `ModelCatalogStore`) and concrete implementations (`EncryptedCredentialStore`, `DataStoreProviderPreferenceStore`, `DataStoreModelCatalogStore`) that features wire together in `MainActivity`.

Features do not depend on implementations — they depend on interfaces. The app shell (`MainActivity`) is the composition root that creates implementations and injects them into feature components via constructors.
