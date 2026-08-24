# Xaero World Map Bridge

Xaero World Map Bridge is a client-side library for mods that draw custom content on [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map). It provides stable, loader-neutral APIs for map overlays and UI overlays on both Fabric and NeoForge.

This project is primarily a dependency for mod developers. Installing it by itself does not add map markers, claims, or other visible content; dependent mods register the overlays that it renders.

## Features

- Ordered map overlays rendered above the map texture and below Xaero's map elements whenever the compatible render hook is available.
- UI overlays rendered above Xaero's interface.
- Map context with camera position, scale, dimensions, and world-to-screen conversion helpers.
- A compatibility fallback for map overlays, with clear diagnostics when Xaero changes its rendering internals.
- Optional Fabric Mod Menu settings for enabling map and UI overlays and the compatibility fallback.

## Installation

1. Install [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map) and the matching Fabric or NeoForge loader for your Minecraft version.
2. Download the Xaero World Map Bridge file that matches both your Minecraft version and loader.
3. On Fabric, also install [Fabric API](https://modrinth.com/mod/fabric-api).
4. Place Xaero World Map Bridge and its required dependencies in the client's `mods` folder.

It is a client-only dependency. It does not need to be installed on a dedicated server unless another mod specifically requires it there.

## Compatibility

| Minecraft release line | Fabric | NeoForge | Guaranteed Xaero's World Map releases |
| --- | --- | --- | --- |
| 1.21.11 | Yes | Experimental | 1.40--1.45 |
| 26.1.2 | Yes | Experimental | 1.40--1.45 |
| 26.2 | Yes | Experimental | 1.41--1.45 |

Xaero's World Map 1.40 was not released for Minecraft 26.2. Minecraft 26.3 is not supported because a compatible Xaero Fabric artifact is unavailable.

These ranges describe versions that are actively verified, not hard upper limits. Later Xaero releases are allowed to
load on a best-effort basis. If a later release changes the verified map-layer hook, the bridge stays loadable and can
use its optional tail fallback. Loader upgrades and later patches within the matching Minecraft release line are also
accepted; use the bridge file for that Minecraft release line and loader.

## For mod developers

Use `XaeroWorldMapBridge.registerMapOverlay(...)` to draw in map coordinates, or `XaeroWorldMapBridge.registerUiOverlay(...)` to draw over Xaero's UI. Registrations are `AutoCloseable`, so close one to unregister its overlay.

```java
XaeroWorldMapBridge.registerMapOverlay("example:claims", 100, context -> {
    int x = context.worldToScreenX(128.0);
    int y = context.worldToScreenY(-64.0);
    context.canvas().fill(x, y, x + 16, y + 16, 0x80FFAA00);
});
```

See the [project README](https://github.com/billstark001/xaero-world-map-bridge#api) for the complete API, build instructions, and compatibility details.

## License

Xaero World Map Bridge is available under the [MIT License](https://github.com/billstark001/xaero-world-map-bridge/blob/main/LICENSE).
