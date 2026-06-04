# Security Policy

## Supported Versions

OpenZone Kotliners is in early development. Security updates apply to the latest `main` branch until stable releases are published.

## Reporting a Vulnerability

If you discover a security issue, please report it privately before public disclosure. Do **not** open a public GitHub Issue with exploit details, credentials, or sensitive data.

For non-security bugs and feature requests, use [GitHub Issues](https://github.com/bengidev/openzone-kotliners/issues) and see [CONTRIBUTING.md](CONTRIBUTING.md).

Include:

- A clear description of the vulnerability
- Steps to reproduce
- Affected platform/version
- Potential impact
- Suggested fix, if available

## Secret Handling

- Never commit API keys, tokens, credentials, or private config.
- Keep `local.properties` and local environment files out of version control.
- Do not hard-code AI provider credentials in Kotlin source.
- Redact secrets from logs, crash reports, screenshots, and issue reports.
- Prefer secure runtime configuration and Android Keystore for sensitive values.

## AI Data Handling

Future AI integrations should clearly define:

- What user data is sent to AI providers
- Which provider processes each request
- How conversation data is stored locally
- How users can delete stored data
- How sensitive context is filtered before model calls
