package io.github.billstark001.xaerobridge.internal;

import io.github.billstark001.xaerobridge.api.MapOverlayContext;
import io.github.billstark001.xaerobridge.api.MapOverlayRenderer;
import io.github.billstark001.xaerobridge.api.OverlayRegistration;
import io.github.billstark001.xaerobridge.api.UiOverlayContext;
import io.github.billstark001.xaerobridge.api.UiOverlayRenderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** Internal registry implementation. */
public final class OverlayRegistry {
    private static final AtomicLong NEXT_SEQUENCE = new AtomicLong();
    private static final ConcurrentHashMap<String, MapEntry> MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UiEntry> UI = new ConcurrentHashMap<>();

    private OverlayRegistry() {}

    public static OverlayRegistration registerMap(String id, int order, MapOverlayRenderer renderer) {
        validateId(id);
        MapEntry entry = new MapEntry(id, order, NEXT_SEQUENCE.getAndIncrement(), renderer);
        if (MAP.putIfAbsent(id, entry) != null) {
            throw new IllegalArgumentException("A map overlay is already registered for " + id);
        }
        return () -> MAP.remove(id, entry);
    }

    public static OverlayRegistration registerUi(String id, int order, UiOverlayRenderer renderer) {
        validateId(id);
        UiEntry entry = new UiEntry(id, order, NEXT_SEQUENCE.getAndIncrement(), renderer);
        if (UI.putIfAbsent(id, entry) != null) {
            throw new IllegalArgumentException("A UI overlay is already registered for " + id);
        }
        return () -> UI.remove(id, entry);
    }

    public static void renderMap(MapOverlayContext context) {
        for (MapEntry entry : sorted(MAP.values())) {
            try {
                entry.renderer.render(context);
            } catch (RuntimeException error) {
                System.err.println("[Xaero World Map Bridge] Map overlay '" + entry.id + "' failed: " + error);
            }
        }
    }

    public static void renderUi(UiOverlayContext context) {
        for (UiEntry entry : sorted(UI.values())) {
            try {
                entry.renderer.render(context);
            } catch (RuntimeException error) {
                System.err.println("[Xaero World Map Bridge] UI overlay '" + entry.id + "' failed: " + error);
            }
        }
    }

    private static <T extends Entry> List<T> sorted(Iterable<T> entries) {
        ArrayList<T> snapshot = new ArrayList<>();
        entries.forEach(snapshot::add);
        snapshot.sort(Comparator.comparingInt(Entry::order).thenComparingLong(Entry::sequence));
        return snapshot;
    }

    private static void validateId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Overlay id must not be blank");
        }
    }

    private interface Entry {
        int order();
        long sequence();
    }

    private record MapEntry(String id, int order, long sequence, MapOverlayRenderer renderer) implements Entry {}
    private record UiEntry(String id, int order, long sequence, UiOverlayRenderer renderer) implements Entry {}
}
