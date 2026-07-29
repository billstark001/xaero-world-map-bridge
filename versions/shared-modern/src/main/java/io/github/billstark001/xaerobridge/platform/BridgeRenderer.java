package io.github.billstark001.xaerobridge.platform;

import io.github.billstark001.xaerobridge.api.MapOverlayContext;
import io.github.billstark001.xaerobridge.api.UiOverlayContext;
import io.github.billstark001.xaerobridge.internal.BridgeSettings;
import io.github.billstark001.xaerobridge.internal.OverlayRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;

/** Shared renderer for Minecraft 26.1.2 and 26.2. */
public final class BridgeRenderer {
    private static boolean exactRendered;

    private BridgeRenderer() {}

    public static void beginPass() { exactRendered = false; }
    public static boolean wasExactRendered() { return exactRendered; }

    public static void renderExact(Screen screen, GuiGraphicsExtractor graphics, int width, int height) {
        exactRendered = true;
        renderMap(screen, graphics, width, height);
    }

    public static void renderTail(Screen screen, GuiGraphicsExtractor graphics, int width, int height) {
        if (!exactRendered && BridgeSettings.isTailFallbackEnabled()) renderMap(screen, graphics, width, height);
        if (BridgeSettings.isUiOverlayEnabled()) OverlayRegistry.renderUi(new UiOverlayContext(graphics::fill, width, height));
    }

    private static void renderMap(Screen screen, GuiGraphicsExtractor graphics, int width, int height) {
        if (!BridgeSettings.isMapOverlayEnabled()) return;
        double cameraX = readNumber(screen, "cameraX");
        double cameraZ = readNumber(screen, "cameraZ");
        double scale = readNumber(screen, "scale");
        if (!Double.isFinite(cameraX) || !Double.isFinite(cameraZ) || !Double.isFinite(scale) || scale <= 0.0D) return;
        double guiScale = Math.max(1.0D, Minecraft.getInstance().getWindow().getGuiScale());
        OverlayRegistry.renderMap(new MapOverlayContext(graphics::fill, width, height, cameraX, cameraZ,
                scale / guiScale, readDimension(screen)));
    }

    private static String readDimension(Screen screen) {
        Object value = readField(screen, "lastViewedDimensionId");
        if (value == null) value = readField(screen, "lastNonNullViewedDimensionId");
        return value == null ? "unknown" : value.toString();
    }

    private static double readNumber(Screen screen, String name) {
        Object value = readField(screen, name);
        return value instanceof Number number ? number.doubleValue() : Double.NaN;
    }

    private static Object readField(Object instance, String name) {
        for (Class<?> type = instance.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(instance);
            } catch (NoSuchFieldException ignored) {
                // Continue with the parent class.
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }
}
