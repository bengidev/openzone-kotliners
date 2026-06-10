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
- **Reasoning level** — the `ComposerReasoningLevel` enum mapped to `reasoning.effort` on the wire.
- **Credential store** — secure storage for the provider API secret (`CredentialStore` / `MutableCredentialStore`).

## Architecture

- Externals code must not import or reference feature UI or component code.
- Feature domain types (e.g. `ChatMessage`) belong in feature packages.
- Feature orchestration clients belong in the owning feature's package.
- Same app target today — package boundaries are the contract until promoted to a library module.

## Dependency injection

Externals exposes interfaces (`CredentialStore`, `ProviderPreferenceStore`) and concrete implementations (`EncryptedCredentialStore`, `DataStoreProviderPreferenceStore`) that features wire together in `MainActivity`.
