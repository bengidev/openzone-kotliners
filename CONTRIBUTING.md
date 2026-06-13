# Contributing to OpenZone Kotliners

Thanks for your interest! Contributions of all kinds are welcome.

This repo tracks work on [GitHub Issues](https://github.com/bengidev/openzone-kotliners/issues) (`bengidev/openzone-kotliners`). Domain language and architecture rules live in [CONTEXT-MAP.md](CONTEXT-MAP.md) and [docs/architecture/](docs/architecture/). Automated agents read [AGENTS.md](AGENTS.md) and [docs/agents/](docs/agents/) for issue-tracker, triage-label, and domain-doc conventions.

## How to Contribute

1. **Fork** the repo and create a branch: `git checkout -b feat/your-feature`
2. **Code** following the existing Kotlin / Compose style
3. **Test** — ensure unit tests pass: `./gradlew :app:testDebugUnitTest`
4. **Commit** with clear messages (Conventional Commits encouraged: `feat:`, `fix:`, `docs:`)
5. **Push** and open a **Pull Request** describing the change and motivation

## Code Style

- Kotlin official style with 4-space indent
- Follow [module layout rules](docs/architecture/modules.md) and [coroutine concurrency rules](docs/architecture/coroutine-concurrency.md)
- Use terms from the relevant [context glossary](CONTEXT-MAP.md) when naming features, types, or issues
- Use Decompose `MutableValue` state and intent methods for feature state; do not add parallel ViewModels
- Jetpack Compose idioms
- Prefer pure-Kotlin domain layer; keep Android SDK imports out of `domain/`
- Keep AI provider code behind an abstraction (no hard-coded vendor calls in views)
- Feature packages (e.g., `onboarding`, `home`, `chat`) belong in internal packages under `app/src/main/java/io/github/bengidev/openzone/`; use layered packages (`domain`, `application`, `infrastructure`, `presenter`)
- Do not commit secrets, API keys, tokens, or local machine config

## Reporting Issues

Open a [GitHub Issue](https://github.com/bengidev/openzone-kotliners/issues/new) with:

- Steps to reproduce
- Expected vs. actual behavior
- Android version + device/emulator

**Security issues**: do not use public issues. Follow [SECURITY.md](SECURITY.md) for private reporting.

### For maintainers: triage labels

Issues use the label vocabulary in [docs/agents/triage-labels.md](docs/agents/triage-labels.md):

| Label | When to apply |
|-------|----------------|
| `needs-triage` | New issue; maintainer has not evaluated it yet |
| `needs-info` | Waiting on the reporter for clarification |
| `ready-for-agent` | Fully specified; safe for an automated agent to pick up |
| `ready-for-human` | Needs human design or implementation |
| `wontfix` | Will not be actioned |

Create these labels in the GitHub repo if they do not exist yet. CLI helpers: [docs/agents/issue-tracker.md](docs/agents/issue-tracker.md).

## Domain & architecture docs

Before changing a feature area, read its `CONTEXT.md` from [CONTEXT-MAP.md](CONTEXT-MAP.md). Check `docs/adr/` for accepted decisions that affect your change. If a listed file is missing, you may still proceed — glossaries and ADRs are added when terms or decisions are settled.

## License

By contributing, you agree your work is licensed under the [MIT License](LICENSE).
