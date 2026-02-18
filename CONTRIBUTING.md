# Contributing to i18n

Thank you for your interest in contributing! This guide covers how to build,
test, and submit changes.

## Prerequisites

- **Java 17** or later
- **Maven** (included with Apache NetBeans, or install separately)

## Building

```sh
mvn clean package
```

## Running Tests

**Classpath mode** (default):

```sh
mvn test
```

**Module mode**:

```sh
mvn test -Ptest-modulepath
```

**Release build** (includes javadoc verification):

```sh
mvn -Prelease clean package javadoc:jar
```

All three must pass before submitting a pull request.

## Code Style

The project follows these conventions:

- **Allman brace style** — opening brace on its own line:

  ```java
  if (condition)
  {
      doSomething();
  }
  ```

- **Apache License 2.0 header** on every source file
- **Javadoc on all public and protected members** — `doclint all` must produce
  zero warnings
- **Yoda-style null checks** — `null == x` rather than `x == null`
- **No wildcard imports** — always use explicit imports
- **4-space indentation, no tabs**

## Adding Dependencies

- Keep **Jackson** on the **2.x** line
- Keep **JUnit** on the **5.x** line
- Check for the latest patch version of any dependency before submitting

## Submitting Changes

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Ensure tests pass in **both** classpath and module mode
5. Ensure javadoc builds with **zero** warnings
6. Open a pull request

## Reporting Issues

Please use [GitHub Issues](../../issues) to report bugs or request features.

## License

All contributions are licensed under the
[Apache License 2.0](LICENSE).
