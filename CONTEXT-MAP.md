# Context map

Multi-context domain documentation for OpenZone. Before changing an area, read its `CONTEXT.md` (when present) for glossary terms and local conventions.

| Context | Glossary | Scope |
| ------- | -------- | ----- |
| **App** | [`app/CONTEXT.md`](app/CONTEXT.md) | App module shell, `MainActivity`, app-wide Compose theme, navigation |
| **Onboarding** | [`app/src/main/java/io/github/bengidev/openzone/onboarding/CONTEXT.md`](app/src/main/java/io/github/bengidev/openzone/onboarding/CONTEXT.md) | Onboarding pages, flow state, repository abstraction, presenter visuals |
| **Home** | — | Welcome shell, composer, particle orb; Decompose `HomeComponent`; palette from iOS `OpenSpacePalette` |

**System-wide ADRs:** [`docs/adr/`](docs/adr/)

Glossary files are added lazily as terminology stabilizes. Agents should not require them to exist before starting work.
