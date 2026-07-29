# Contributing

## Before submitting

Run the loader-neutral suite and the complete build matrix:

```powershell
.\gradlew.bat testAll
.\gradlew.bat clean buildAll --no-parallel
```

Changes to a Mixin anchor, renderer adapter, Minecraft version, Xaero version, or loader configuration must also pass:

```powershell
.\scripts\verify-xaero-artifacts.ps1
.\gradlew.bat localIntegration
```

`localIntegration` launches graphical Minecraft clients and is not required on GitHub-hosted CI. Include the relevant client log and failing screenshot when reporting an integration failure.

## Commit messages

Use an imperative `<type>: <summary>` subject. Keep the first line concise and put motivation, compatibility notes, or migration details in the body.

Project commit types are:

- `feat:` user-visible behavior or API capability.
- `fix:` defect and compatibility corrections.
- `add:` new tests, tooling, fixtures, or other additive support material.
- `docs:` documentation-only changes.
- `refactor:` behavior-preserving code restructuring.
- `test:` corrections to existing tests.
- `build:` Gradle, dependency, packaging, or release-build changes.
- `ci:` GitHub Actions and other automation changes.
- `chore:` maintenance that fits none of the above.

Breaking changes append `!`, for example `feat!: replace the overlay canvas contract`, and explain the migration in the commit body. This follows the Conventional Commits structure with `add` retained as a project-specific additive type.

Examples:

```text
fix: require the stable Xaero render hooks
add: cover overlay rendering with client game tests
docs: document canonical dimension keys
```

Keep each commit focused. Do not include generated client worlds, logs, screenshots, or Gradle build output in commits.

## Changelog

`CHANGELOG.md` follows Keep a Changelog:

- Add every user-visible or contributor-visible change under `Unreleased`.
- Use the standard `Added`, `Changed`, `Deprecated`, `Removed`, `Fixed`, and `Security` sections.
- Move `Unreleased` entries into a dated version section when releasing.
- Link each released version to its Git comparison or tag.
- Do not use the Git commit log as the release changelog.

Pure formatting changes and internal refactors require no changelog entry unless they affect contributors, builds, supported environments, or diagnostics.
