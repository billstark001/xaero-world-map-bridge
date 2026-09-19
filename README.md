# Xaero World Map Bridge

Xaero World Map Bridge is a client-side compatibility library that provides loader-neutral map and UI overlay APIs for Xaero's World Map.

## API

`XaeroWorldMapBridge.registerMapOverlay(id, order, renderer)` renders above the map texture and before Xaero's map elements and UI when the exact injection anchor is available.

`XaeroWorldMapBridge.registerUiOverlay(id, order, renderer)` renders at the end of Xaero's screen rendering and above Xaero's UI.

Both callbacks receive a loader-neutral `OverlayCanvas` for filled rectangles. Map callbacks also receive the current
camera, scale, dimension string, and world-to-screen helper methods. A registration is an `AutoCloseable`; close it to
unregister.

Canvas coordinates follow Xaero's own screen convention: GUI-scaled pixels, `(0, 0)` at the top-left, positive X to
the right, and positive Y down. Rectangle right/bottom bounds are exclusive, as with Minecraft's GUI drawing API.

`MapOverlayContext.dimension()` is a canonical Minecraft dimension key in `namespace:path` form, for example `minecraft:overworld` or `minecraft:the_nether`. It returns `unknown` only when Xaero has not exposed a viewed dimension.

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

### Native Xaero elements and widgets

Xaero 1.46 already exposes its map-element pipeline, so the bridge does not wrap it with a second, competing API.
Mods that need text, textures, hover handling, right-click actions, tooltips, or Xaero-managed labels should compile
directly against Xaero's published development artifact and register their `ElementRenderer` with:

```groovy
repositories {
    maven {
        name = "Xaero's Maven"
        url = "https://chocolateminecraft.com/maven"
    }
}

dependencies {
    // Recommended for code shared by Fabric and NeoForge targets.
    compileOnly "xaero.map:xaeroworldmap-common-${minecraft_version}:${xaero_world_map_version}"
    // A loader-specific source set can instead use xaeroworldmap-fabric-* or xaeroworldmap-neoforge-*.
}
```

The coordinates above follow Xaero's [official developer setup](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map#for-developers).
At runtime, register the renderer after Xaero's client setup:

```java
WorldMap.mapElementRenderHandler.add(renderer);
```

Register after Xaero's client initialization. Supply world positions from `ElementReader.getRenderX` and
`getRenderZ`, handle `ElementRenderLocation.WORLD_MAP`, and use `ElementRenderer.getOrder()` for Xaero's native
ordering. The `MapElementGraphics` passed to the renderer provides `drawString`, `drawCenteredString`, `blit`,
`fill`, and `fillGradient`; its transformed origin and scale follow Xaero's own element positioning. These are Xaero
APIs, so consumers should pin and test the Xaero version they compile against.

For ordinary interactive controls, use the loader's screen-initialization event, check for `GuiMap`, and call its
public `addRenderableWidget(...)` method with a vanilla widget such as `Button`. Position these in the same top-left
GUI coordinate space described above. The bridge's render callback is intended for drawing only; it deliberately does
not duplicate Minecraft's focus, input, narration, and widget lifecycle.

## Injection policy

The bridge has two map-layer paths:

1. The exact Mixin injection runs immediately before Xaero's `MapElementRenderHandler.render` call. This is the default path and keeps map overlays below Xaero's waypoints, menus, tooltips, and other UI.
2. The tail injection is an opt-in compatibility fallback. Map overlays rendered through this path appear above Xaero's UI. UI overlays always render at the tail.

Fabric exposes `Map overlay`, `UI overlay`, and `Map injection` settings through Mod Menu when it is installed. Mod Menu is an optional Fabric integration. NeoForge artifacts use the same persisted properties file and do not provide Mod Menu integration.

The pass-start and pass-tail hooks are bytecode-verified on every guaranteed target. All injections remain optional at
load time so an unverified future Xaero release cannot abort startup solely because an internal method changed. The
exact map-layer hook may be absent so the tail fallback remains available. In exact-only mode, the bridge reports the
first missing exact hook when map overlays are registered.

## Supported targets

| Minecraft release line | Fabric | NeoForge | Guaranteed Xaero World Map releases |
| --- | --- | --- | --- |
| 1.21.11 | Yes | Experimental | 1.40--1.46 |
| 26.1.2 | Yes | Experimental | 1.40--1.46 |
| 26.2 | Yes | Experimental | 1.41--1.46 |
| 26.3 | Yes | Experimental | 1.46 |

Xaero World Map 1.40 was not published for Minecraft 26.2. Xaero 1.46 is the first release line published for
Minecraft 26.3.

The table is the bytecode-verified guarantee, not a hard maximum. Newer Xaero releases are allowed to load on the
matching Minecraft artifact and are handled on a best-effort basis. If an unverified Xaero release changes the exact
map-layer anchor, the bridge leaves that hook inactive instead of failing startup; users can opt into the tail fallback.
Fabric Loader and Java likewise have minimum-only requirements. Minecraft and NeoForge predicates accept later patch
versions within the same Minecraft release line, while keeping separate binary-incompatible release lines in separate
JARs.

## Build

```powershell
.\gradlew.bat :versions:fabric-1.21.11:build
.\gradlew.bat :versions:fabric-26.1.2:build
.\gradlew.bat :versions:fabric-26.2:build
.\gradlew.bat :versions:fabric-26.3:build

.\gradlew.bat :versions:neoforge-1.21.11:build
.\gradlew.bat :versions:neoforge-26.1.2:build
.\gradlew.bat :versions:neoforge-26.2:build
.\gradlew.bat :versions:neoforge-26.3:build
```

`buildAll` builds the complete eight-target matrix. Each output is stored in its target module's `build/libs` directory.

For a Modrinth release, the following command builds every target and collects the eight distributable JARs in the root
`build/modrinth` directory. Source, development, and Javadoc JARs are excluded.

```powershell
.\scripts\build-modrinth.ps1
```

### Development run configurations

Gradle creates separate development configurations for every Minecraft and loader target. All targets intentionally
share the root `run/` directory, while their module and loader-specific launch data remain isolated. Fabric IntelliJ
configurations explicitly use Java 21 for Minecraft 1.21.11 and Java 25 for Minecraft 26.x. NeoForge uses the Java
toolchain declared by its Gradle subproject.

Reloading the Gradle project refreshes Fabric IntelliJ configurations automatically. They can also be rebuilt with:

```powershell
.\gradlew.bat syncIdeaRunConfigurations
```

The refresh removes old generated Fabric entries before recreating them, so deleted targets do not remain in IDEA.
When adding or removing a Minecraft target:

1. Update its metadata entry in `build.gradle`.
2. Update both matching `versions/fabric-<minecraft>` and `versions/neoforge-<minecraft>` projects in
   `settings.gradle`, together with their source directories.
3. Update `.github/workflows`, `scripts/run-local-neoforge-smoke.ps1`, and the Xaero artifact verification matrix.
4. Reload the Gradle project, or run `syncIdeaRunConfigurations` for Fabric IDEA entries. Reload VS Code to refresh
   NeoForge entries generated by ModDevGradle; after deleting a target, remove its obsolete NeoForge entry from the
   tracked `.vscode/launch.json` if the Java extension retains it.

`buildAll` and the Fabric portion of `localIntegration` derive their project lists from `targets`, so they do not need
separate updates.

## Tests

Loader-neutral unit tests cover coordinate conversion, registry ordering and isolation, registration lifecycle,
settings persistence, and distribution metadata:

```powershell
.\gradlew.bat testAll
```

The local graphical integration suite runs a Fabric Client GameTest for every Minecraft target. Each test creates an isolated single-player world, opens Xaero's map, verifies both callbacks and their contexts, checks enable and disable behavior and unregistration, and asserts that both overlay colors are present in a screenshot. The suite also starts every NeoForge target to verify mod discovery and a clean client startup:

```powershell
.\gradlew.bat localIntegration
```

Fabric screenshots are stored under each target's `build/run/clientGameTest/screenshots` directory. NeoForge startup checks can also be run separately:

```powershell
.\gradlew.bat localNeoForgeSmoke
```

Client integration tests are not part of GitHub Actions because they require a graphical Minecraft client. Unit tests, all eight builds, and published Xaero anchor verification are automated in CI.

## Xaero compatibility verification

Xaero does not document the map-layer insertion point as a public API. Exact hooks are maintained through bytecode verification of published Xaero artifacts.

Use this script to disassemble a supplied Xaero JAR:

```powershell
.\scripts\disassemble-xaero.ps1 -Jar path\to\xaeroworldmap.jar
```

It stores `GuiMap.javap.txt` under `build/xaero-inspection`. To verify every published artifact in the guaranteed 1.40--1.46 matrix, run:

```powershell
.\scripts\verify-xaero-artifacts.ps1
```

The verification script downloads artifacts from Modrinth, disassembles `xaero.map.gui.GuiMap` with `javap`, locates the relevant render method, and requires exactly one complete invocation descriptor matching the adapter's Mixin anchor. CI runs this verification on a schedule and on demand.

## CI/CD

GitHub Actions builds each Fabric and NeoForge target independently. A separate workflow validates every published
Xaero artifact in the guaranteed range. Tags such as `v0.1.1` validate the version and changelog, build the complete
matrix once, and publish all eight JARs to a GitHub Release. When the `MODRINTH_PROJECT_ID` repository variable and
`MODRINTH_TOKEN` secret are configured, the same workflow also publishes a separate Fabric or NeoForge Modrinth
version for every Minecraft target. The workflow can be dispatched manually against an existing tag to retry a
release; Modrinth retrying is opt-in for manual runs.

Contribution, changelog, and commit-message conventions are documented in [`CONTRIBUTING.md`](CONTRIBUTING.md). User-visible changes are recorded in [`CHANGELOG.md`](CHANGELOG.md).
