# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.4.x   | ✓ Yes     |
| < 1.4   | ✗ No      |

## Reporting a Vulnerability

Please **do not** open a public GitHub issue for security vulnerabilities.

Report vulnerabilities privately via [GitHub's private vulnerability reporting](https://github.com/clydegerber/i18n/security/advisories/new).

You can expect:
- **Acknowledgement** within 5 business days
- **Status update** within 15 business days
- A fix or mitigation plan communicated before any public disclosure

## Security Considerations

`i18n-core` loads resource bundles from the classpath and deserializes JSON/XML files. Ensure that resource bundle files in your application are sourced from trusted locations only.
