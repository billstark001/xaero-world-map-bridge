# Xaero World Map Bridge

Xaero World Map Bridge is a small, client-side experiment that gives other mods
two stable callback APIs for Xaero's World Map.

## 0.1.0 API

`XaeroWorldMapBridge.registerMapOverlay(id, order, renderer)` renders above the
map texture and before Xaero's map elements and UI when the exact injection
anchor is available.

`XaeroWorldMapBridge.registerUiOverlay(id, order, renderer)` renders at the end
of Xaero's screen rendering and is intentionally above Xaero's UI.

Both callbacks receive a loader-neutral `OverlayCanvas`. Map callbacks also
receive the current camera, scale, dimension string, and world-to-screen helper
methods. A registration is an `AutoCloseable`; close it to unregister.

`MapOverlayContext.dimension()` is a canonical Minecraft dimension key in
`namespace:path` form, for example `minecraft:overworld` or
`minecraft:the_nether`. It returns `unknown` only when Xaero has not exposed a
viewed dimension yet.

```java
XaeroWorldMapBridge.registerMapOverlay("example:claims", 100, context -> {
    int x = context.worldToScreenX(128.0);
    int y = context.worldToScreenY(-64.0);
    context.canvas().fill(x, y, x + 16, y + 16, 0x80FFAA00);
});

XaeroWorldMapBridge.registerUiOverlay("example:badge", 0, context -> {
    context.canvas().fill(4, 4, 84, 18, 0xCC202020);
});
```

## Injection policy

The bridge has two map-layer paths:

1. The exact mixin injection runs immediately before Xaero's
   `MapElementRenderHandler.render` call. This is the normal path and keeps map
   overlays below Xaero's waypoints, menus, tooltips, and other UI.
2. The tail injection is an opt-in fallback. It is useful when Xaero changes an
   internal anchor, but map overlays will then be above Xaero's UI. UI overlays
   always render at the tail by design.

Fabric exposes `Map overlay`, `UI overlay`, and `Map injection` in Mod Menu when
Mod Menu is installed. Mod Menu is optional and Fabric-only; NeoForge artifacts
use the same persisted properties file but intentionally do not claim a Mod
Menu integration.

The pass-start and pass-tail hooks are required on every supported target. The
exact map-layer hook is allowed to miss so tail fallback remains possible. In
exact-only mode, the bridge reports the first missing exact hook instead of
silently dropping registered map overlays.

## Supported targets

| Minecraft | Fabric | NeoForge | Xaero World Map releases |
| --- | --- | --- | --- |
| 1.21.11 | Yes | Experimental | 1.40--1.44 |
| 26.1.2 | Yes | Experimental | 1.40--1.44 |
| 26.2 | Yes | Experimental | 1.41--1.44 |

Xaero did not publish a 1.40 build for Minecraft 26.2. There is currently no
published Xaero Fabric build for Minecraft 26.3, so it is intentionally absent.

## Build

```powershell
.\gradlew.bat :versions:fabric-1.21.11:build
.\gradlew.bat :versions:fabric-26.1.2:build
.\gradlew.bat :versions:fabric-26.2:build

.\gradlew.bat :versions:neoforge-1.21.11:build
.\gradlew.bat :versions:neoforge-26.1.2:build
.\gradlew.bat :versions:neoforge-26.2:build
```

`buildAll` builds the complete six-target matrix. Each output is kept in its
target module's `build/libs` directory.

## Tests

Loader-neutral unit tests cover coordinate conversion, registry ordering and
isolation, registration lifecycle, settings persistence, and distribution
metadata:

```powershell
.\gradlew.bat testAll
```

The graphical integration suite is intentionally local-only. It runs a Fabric
Client GameTest for every Minecraft target, creates an isolated single-player
world, opens Xaero's map, verifies both callbacks and their contexts, checks
enable/disable and unregister behavior, and asserts that both overlay colors
are present in a screenshot. It then starts every NeoForge target long enough
to verify mod discovery and a clean client startup:

```powershell
.\gradlew.bat localIntegration
```

Fabric screenshots are stored under each target's
`build/run/clientGameTest/screenshots` directory. NeoForge startup checks can
also be run separately:

```powershell
.\gradlew.bat localNeoForgeSmoke
```

Client integration tests are not part of GitHub Actions because they require a
graphical Minecraft client. Unit tests, all six builds, and published Xaero
anchor verification remain automated in CI.

## Xaero compatibility verification

Xaero does not document the map-layer insertion point as a public API. The
exact hooks are therefore maintained from bytecode evidence, not guesswork.

Use this script to disassemble one supplied Xaero JAR:

```powershell
.\scripts\disassemble-xaero.ps1 -Jar path\to\xaeroworldmap.jar
```

It stores `GuiMap.javap.txt` under `build/xaero-inspection`. To verify every
published artifact in the supported 1.40--1.44 matrix, run:

```powershell
.\scripts\verify-xaero-artifacts.ps1
```

The verification script downloads the artifacts from Modrinth, disassembles
`xaero.map.gui.GuiMap` with `javap`, locates the relevant render method, and
requires exactly one complete invocation descriptor matching the adapter's
Mixin anchor. CI runs it on a schedule and on demand.

## CI/CD

GitHub Actions builds each Fabric and NeoForge target independently. A second
workflow validates every published Xaero artifact. Tagging a version such as
`v0.1.0` builds the matrix and uploads all JARs to a GitHub release.

Contribution, changelog, and commit-message conventions are documented in
[`CONTRIBUTING.md`](CONTRIBUTING.md). User-visible changes are recorded in
[`CHANGELOG.md`](CHANGELOG.md).
