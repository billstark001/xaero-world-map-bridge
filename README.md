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

Fabric exposes `Map overlay`, `UI overlay`, and `Map injection` in Mod Menu.
Mod Menu is Fabric-only; NeoForge artifacts use the same persisted properties
file but intentionally do not claim a Mod Menu integration.

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
`xaero.map.gui.GuiMap` with `javap`, and checks the exact anchor expected by
each Minecraft/loader adapter. CI runs it on a schedule and on demand.

## CI/CD

GitHub Actions builds each Fabric and NeoForge target independently. A second
workflow validates every published Xaero artifact. Tagging a version such as
`v0.1.0` builds the matrix and uploads all JARs to a GitHub release.
