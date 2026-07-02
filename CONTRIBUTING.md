# Contributing to Wangdu

Thanks for your interest in contributing. This document explains how to set up the project, propose changes, and get them merged.

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## Ways to Contribute

- Report bugs by opening an issue with clear reproduction steps
- Suggest features or improvements through an issue
- Improve documentation
- Submit code changes through a pull request

## Getting Started

1. Fork the repository and clone your fork
2. Open the project in Android Studio or IntelliJ IDEA
3. Sync Gradle
4. Run the server with `./gradlew :server:run` and a client target to verify your setup

See the [README](README.md) for full setup and run instructions across platforms.

## Branching and Commits

- Create a feature branch off `main` (for example `feature/live-eraser` or `fix/cursor-lag`)
- Keep each pull request focused on a single concern
- Write clear, imperative commit messages (for example `Add reconnect with exponential backoff`)

## Development Guidelines

- Follow the existing Kotlin and Compose conventions in the codebase
- Keep shared logic in the `shared` module and platform specific code in its target
- Prefer expressive names over comments; add comments only where intent is not obvious
- Do not commit generated files, local databases (`*.db`), or IDE settings

## Testing

- Add or update tests for any behavior you change
- Run the test suite before opening a pull request:

```bash
./gradlew test
```

- Confirm the affected client targets and the server still build and run

## Pull Request Process

1. Ensure your branch is up to date with `main`
2. Make sure the project builds and all tests pass
3. Update documentation if your change affects usage or setup
4. Open a pull request with a clear description of the change and the motivation behind it
5. Link any related issues
6. Be responsive to review feedback

## Reporting Bugs

When filing a bug report, please include:

- A clear description of the problem
- Steps to reproduce
- Expected versus actual behavior
- Platform and version details (Android, iOS, or Desktop)
- Relevant logs or screenshots

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE) that covers this project.
