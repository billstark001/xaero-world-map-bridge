package io.github.billstark001.xaerobridge.gametest;

import io.github.billstark001.xaerobridge.api.MapOverlayContext;
import io.github.billstark001.xaerobridge.api.OverlayRegistration;
import io.github.billstark001.xaerobridge.api.UiOverlayContext;
import io.github.billstark001.xaerobridge.api.XaeroWorldMapBridge;
import io.github.billstark001.xaerobridge.internal.BridgeSettings;
import io.github.billstark001.xaerobridge.platform.BridgeRenderer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.Screen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UnstableApiUsage")
public final class XaeroBridgeClientGameTest implements FabricClientGameTest {
    private static final int MAP_COLOR = 0xFFFF00FF;
    private static final int UI_COLOR = 0xFF00FF00;

    @Override
    public void runTest(ClientGameTestContext test) {
        BridgeSettings.initialize(null);
        AtomicInteger mapCalls = new AtomicInteger();
        AtomicInteger uiCalls = new AtomicInteger();
        AtomicReference<MapOverlayContext> lastMapContext = new AtomicReference<>();
        AtomicReference<UiOverlayContext> lastUiContext = new AtomicReference<>();

        OverlayRegistration mapRegistration = XaeroWorldMapBridge.registerMapOverlay(
                "xaero_bridge_gametest:map", Integer.MIN_VALUE, context -> {
                    lastMapContext.set(context);
                    mapCalls.incrementAndGet();
                    int x = context.width() / 2 + 80;
                    int y = context.height() / 2 + 60;
                    context.canvas().fill(x, y, x + 32, y + 32, MAP_COLOR);
                });
        OverlayRegistration uiRegistration = XaeroWorldMapBridge.registerUiOverlay(
                "xaero_bridge_gametest:ui", Integer.MAX_VALUE, context -> {
                    lastUiContext.set(context);
                    uiCalls.incrementAndGet();
                    context.canvas().fill(4, 4, 36, 36, UI_COLOR);
                });

        try (mapRegistration; uiRegistration;
             TestSingleplayerContext ignored = test.worldBuilder().setUseConsistentSettings(true).create()) {
            test.waitFor(client -> client.level != null, 600);
            test.waitTicks(20);
            test.getInput().pressKey(77);
            test.waitForScreen(Screen.class);
            test.waitFor(client -> mapCalls.get() >= 3 && uiCalls.get() >= 3, 400);

            assertContext(lastMapContext.get(), lastUiContext.get());
            if (!BridgeRenderer.wasExactRendered()) {
                throw new AssertionError("The exact Xaero injection did not render");
            }

            Path screenshot = test.takeScreenshot("xaero-bridge-overlays");
            assertScreenshotContains(screenshot, MAP_COLOR, "map overlay");
            assertScreenshotContains(screenshot, UI_COLOR, "UI overlay");

            int mapBeforeDisable = mapCalls.get();
            int uiBeforeDisable = uiCalls.get();
            BridgeSettings.setMapOverlayEnabled(false);
            BridgeSettings.setUiOverlayEnabled(false);
            test.waitTicks(10);
            assertEquals(mapBeforeDisable, mapCalls.get(), "disabled map overlay still rendered");
            assertEquals(uiBeforeDisable, uiCalls.get(), "disabled UI overlay still rendered");

            // Let Xaero finish its screen lifecycle before the test world is
            // closed. Disconnecting with GuiMap still open can deadlock the
            // 26.1.2 client while its integrated server is shutting down.
            test.getInput().pressKey(256);
            test.waitTicks(2);
        } finally {
            BridgeSettings.initialize(null);
        }

        int mapAfterClose = mapCalls.get();
        int uiAfterClose = uiCalls.get();
        test.waitTicks(5);
        assertEquals(mapAfterClose, mapCalls.get(), "closed map registration still rendered");
        assertEquals(uiAfterClose, uiCalls.get(), "closed UI registration still rendered");
    }

    private static void assertContext(MapOverlayContext map, UiOverlayContext ui) {
        if (map == null || ui == null) throw new AssertionError("overlay context was not captured");
        if (!Double.isFinite(map.cameraX()) || !Double.isFinite(map.cameraZ())) {
            throw new AssertionError("camera coordinates are not finite");
        }
        if (!Double.isFinite(map.pixelsPerBlock()) || map.pixelsPerBlock() <= 0.0) {
            throw new AssertionError("pixelsPerBlock is invalid: " + map.pixelsPerBlock());
        }
        if (!"minecraft:overworld".equals(map.dimension())) {
            throw new AssertionError("unexpected dimension key: " + map.dimension());
        }
        assertEquals((int) Math.round(map.width() / 2.0), map.worldToScreenX(map.cameraX()),
                "camera X does not map to screen center");
        assertEquals((int) Math.round(map.height() / 2.0), map.worldToScreenY(map.cameraZ()),
                "camera Z does not map to screen center");
        assertEquals(map.width(), ui.width(), "map and UI widths differ");
        assertEquals(map.height(), ui.height(), "map and UI heights differ");
    }

    private static void assertScreenshotContains(Path screenshot, int expectedArgb, String label) {
        try {
            BufferedImage image = ImageIO.read(screenshot.toFile());
            int expectedRgb = expectedArgb & 0x00FFFFFF;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) & 0x00FFFFFF) == expectedRgb) return;
                }
            }
        } catch (IOException error) {
            throw new AssertionError("failed to read integration screenshot " + screenshot, error);
        }
        throw new AssertionError(label + " color was not present in " + screenshot);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
