# Contributing to OpenZone Kotliners

Welcome! Contributions, ideas, and issues are appreciated.

## How to Contribute

1. **Fork** the repo and create a branch: `git checkout -b feat/your-feature`
2. **Code** following the existing Kotlin/Jetpack Compose style
3. **Test** — ensure unit and instrumented tests pass
4. **Commit** with clear messages (Conventional Commits encouraged: `feat:`, `fix:`, `docs:`)
5. **Push** and open a **Pull Request** describing the change and motivation

## Code Style

- Kotlin official style
- Jetpack Compose idioms
- Prefer small, composable UI functions
- Keep AI provider code behind an abstraction; no hard-coded vendor calls in views
- Feature areas (e.g. onboarding) belong in internal packages under `app/src/main/java/io/github/bengidev/openzone/`; use layered packages (`domain`, `application`, `infrastructure`, `presenter`) and repository interfaces for persistence
- Do not commit secrets, API keys, tokens, or local machine config

## Reporting Issues

Open a [GitHub Issue](https://github.com/bengidev/openzone-kotliners/issues/new) with:

- Steps to reproduce
- Expected vs actual behavior
- Android version + device/emulator

Maintainers may apply [triage labels](docs/agents/triage-labels.md) (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`) to track issue state. See [docs/agents/issue-tracker.md](docs/agents/issue-tracker.md) for CLI conventions.

**Security issues:** follow [SECURITY.md](SECURITY.md) and report privately; do not file public issues with exploit details or secrets.

## Documentation for contributors

| Topic | Location |
| ----- | -------- |
| Agent/workflow config | [docs/agents/](docs/agents/) |
| Domain contexts | [CONTEXT-MAP.md](CONTEXT-MAP.md) |
| Architecture decisions | [docs/adr/](docs/adr/) |
| Agent coding rules | [AGENTS.md](AGENTS.md) |

## License

By contributing, you agree your work is licensed under the [MIT License](LICENSE).
