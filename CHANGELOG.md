# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-07-29

### Added

- A loader-neutral client API for registering ordered map and UI overlays on Xaero's World Map.
- Map overlay contexts with camera coordinates, map scale, screen dimensions, world-to-screen conversion, and canonical `namespace:path` dimension keys.
- UI overlay contexts for drawing above Xaero's interface.
- Closeable overlay registrations with deterministic ordering, replacement by identifier, explicit unregistration, and per-renderer failure isolation.
- An exact map-layer injection immediately before Xaero renders map elements, plus an opt-in tail fallback for compatibility diagnostics.
- Fabric and NeoForge artifacts for Minecraft 1.21.11, 26.1.2, and 26.2, covering the documented Xaero World Map 1.40--1.44 compatibility matrix.
- Optional Fabric Mod Menu configuration for enabling map overlays, UI overlays, and the tail fallback.
- Persistent settings with atomic loading, strict validation, predictable defaults, and support for parentless configuration paths.
- One-time diagnostics for missing exact hooks and renderer failures.
- Loader-neutral JUnit tests for contexts, registrations, renderer isolation, settings persistence, and distribution metadata.
- Local Fabric Client GameTests that open Xaero's map and verify callback contexts, registration lifecycle, enable and disable behavior, and rendered screenshot pixels on every supported Minecraft target.
- Local NeoForge client startup smoke tests for every supported Minecraft target.
- Automated builds for the complete Fabric and NeoForge target matrix.
- Automated bytecode verification of the exact injection anchor across every published Xaero artifact in the supported release matrix.
- Contributor documentation for testing, Conventional Commits, and Keep a Changelog.

[Unreleased]: https://github.com/billstark001/xaero-world-map-bridge/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/billstark001/xaero-world-map-bridge/releases/tag/v0.1.0
