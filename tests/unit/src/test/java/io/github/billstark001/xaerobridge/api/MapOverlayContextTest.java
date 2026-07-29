package io.github.billstark001.xaerobridge.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapOverlayContextTest {
    private static final OverlayCanvas NO_OP_CANVAS = (left, top, right, bottom, argb) -> {};

    @Test
    void convertsWorldCoordinatesAroundTheCamera() {
        MapOverlayContext context = new MapOverlayContext(
                NO_OP_CANVAS, 801, 601, 100.0, -50.0, 2.5, "minecraft:overworld");

        assertEquals(401, context.worldToScreenX(100.0));
        assertEquals(301, context.worldToScreenY(-50.0));
        assertEquals(426, context.worldToScreenX(110.0));
        assertEquals(276, context.worldToScreenY(-60.0));
    }

    @Test
    void usesJavaRoundSemanticsForSubpixelCoordinates() {
        MapOverlayContext context = new MapOverlayContext(
                NO_OP_CANVAS, 800, 600, 0.0, 0.0, 0.5, "minecraft:the_nether");

        assertEquals(401, context.worldToScreenX(1.0));
        assertEquals(400, context.worldToScreenX(-1.0));
        assertEquals("minecraft:the_nether", context.dimension());
    }

    @Test
    void normalizesNullDimensionAndRejectsNullCanvas() {
        MapOverlayContext context = new MapOverlayContext(NO_OP_CANVAS, 1, 1, 0, 0, 1, null);

        assertEquals("unknown", context.dimension());
        assertThrows(NullPointerException.class,
                () -> new MapOverlayContext(null, 1, 1, 0, 0, 1, "minecraft:overworld"));
    }
}
