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
- Do not commit secrets, API keys, tokens, or local machine config

## Reporting Issues

Open a GitHub Issue with:

- Steps to reproduce
- Expected vs actual behavior
- Android version + device/emulator

## License

By contributing, you agree your work is licensed under the [MIT License](LICENSE).
