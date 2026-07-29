package io.github.billstark001.xaerobridge.internal;

import io.github.billstark001.xaerobridge.api.MapOverlayContext;
import io.github.billstark001.xaerobridge.api.OverlayCanvas;
import io.github.billstark001.xaerobridge.api.OverlayRegistration;
import io.github.billstark001.xaerobridge.api.UiOverlayContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverlayRegistryTest {
    private static final OverlayCanvas NO_OP_CANVAS = (left, top, right, bottom, argb) -> {};

    @Test
    void rendersByOrderThenRegistrationSequence() {
        String prefix = UUID.randomUUID().toString();
        List<String> calls = new ArrayList<>();
        try (OverlayRegistration second = OverlayRegistry.registerMap(prefix + ":second", 20, context -> calls.add("second"));
             OverlayRegistration first = OverlayRegistry.registerMap(prefix + ":first", 10, context -> calls.add("first"));
             OverlayRegistration third = OverlayRegistry.registerMap(prefix + ":third", 20, context -> calls.add("third"))) {
            OverlayRegistry.renderMap(mapContext());
        }

        assertEquals(List.of("first", "second", "third"), calls);
    }

    @Test
    void rejectsDuplicateAndInvalidIds() {
        String id = UUID.randomUUID() + ":duplicate";
        try (OverlayRegistration registration = OverlayRegistry.registerMap(id, 0, context -> {})) {
            assertThrows(IllegalArgumentException.class,
                    () -> OverlayRegistry.registerMap(id, 1, context -> {}));
        }
        assertThrows(IllegalArgumentException.class,
                () -> OverlayRegistry.registerMap("   ", 0, context -> {}));
        assertThrows(NullPointerException.class,
                () -> OverlayRegistry.registerMap(null, 0, context -> {}));
    }

    @Test
    void closeIsIdempotentAndRemovesTheOverlay() {
        AtomicInteger calls = new AtomicInteger();
        OverlayRegistration registration = OverlayRegistry.registerUi(
                UUID.randomUUID() + ":ui", 0, context -> calls.incrementAndGet());

        OverlayRegistry.renderUi(new UiOverlayContext(NO_OP_CANVAS, 100, 100));
        registration.close();
        registration.close();
        OverlayRegistry.renderUi(new UiOverlayContext(NO_OP_CANVAS, 100, 100));

        assertEquals(1, calls.get());
    }

    @Test
    void isolatesRendererFailuresAndKeepsRendering() {
        String prefix = UUID.randomUUID().toString();
        AtomicInteger healthyCalls = new AtomicInteger();
        try (OverlayRegistration broken = OverlayRegistry.registerMap(
                prefix + ":broken", 0, context -> { throw new IllegalStateException("boom"); });
             OverlayRegistration healthy = OverlayRegistry.registerMap(
                     prefix + ":healthy", 1, context -> healthyCalls.incrementAndGet())) {
            OverlayRegistry.renderMap(mapContext());
            OverlayRegistry.renderMap(mapContext());
        }

        assertEquals(2, healthyCalls.get());
    }

    @Test
    void takesASnapshotBeforeDispatch() {
        String prefix = UUID.randomUUID().toString();
        List<String> calls = new ArrayList<>();
        OverlayRegistration[] later = new OverlayRegistration[1];
        try (OverlayRegistration first = OverlayRegistry.registerMap(prefix + ":first", 0, context -> {
            calls.add("first");
            later[0].close();
        })) {
            later[0] = OverlayRegistry.registerMap(prefix + ":later", 1, context -> calls.add("later"));
            OverlayRegistry.renderMap(mapContext());
            OverlayRegistry.renderMap(mapContext());
        } finally {
            if (later[0] != null) later[0].close();
        }

        assertEquals(List.of("first", "later", "first"), calls);
        assertFalse(OverlayRegistry.hasMapOverlays());
    }

    @Test
    void reportsWhetherMapOverlaysExist() {
        assertFalse(OverlayRegistry.hasMapOverlays());
        try (OverlayRegistration ignored = OverlayRegistry.registerMap(
                UUID.randomUUID() + ":present", 0, context -> {})) {
            assertTrue(OverlayRegistry.hasMapOverlays());
        }
        assertFalse(OverlayRegistry.hasMapOverlays());
    }

    private static MapOverlayContext mapContext() {
        return new MapOverlayContext(NO_OP_CANVAS, 100, 100, 0, 0, 1, "minecraft:overworld");
    }
}
