# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Loader-neutral JUnit tests for contexts, registrations, renderer isolation,
  settings persistence, and distribution metadata.
- Local Fabric Client GameTests that open Xaero's map and verify callback,
  context, lifecycle, enable/disable, and screenshot behavior on every target.
- Local NeoForge client startup smoke tests.
- Contributor guidance for tests, Conventional Commits, and changelog entries.

### Changed

- `MapOverlayContext.dimension()` now returns a canonical `namespace:path`
  dimension key.
- Minecraft dependency declarations now match the exact versions compiled and
  tested by each artifact.
- Mod Menu is an optional Fabric integration instead of a hard dependency.
- Published Xaero verification now checks one exact invocation descriptor in
  the relevant render method.

### Fixed

- Stable pass-start and pass-tail Mixin hooks can no longer fail silently.
- Fabric 1.21.11 development and client-test runs now expose Xaero's embedded
  XaeroLib dependency after Loom remapping.
- A missing exact map hook is reported once when exact-only overlays are
  registered.
- Repeated renderer failures no longer flood stderr every frame.
- Settings loading is atomic, malformed booleans are rejected, defaults reset
  predictably, null injection modes are rejected, and parentless save paths are
  supported.

[Unreleased]: https://github.com/billstark001/xaero-world-map-bridge/compare/main...HEAD
