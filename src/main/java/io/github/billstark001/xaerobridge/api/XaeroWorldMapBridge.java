package io.github.billstark001.xaerobridge.api;

import io.github.billstark001.xaerobridge.internal.OverlayRegistry;

import java.util.Objects;

/**
 * Public API of Xaero World Map Bridge.
 *
 * <p>This release intentionally exposes only two extension points: map layers
 * and UI layers. Both are client-side and must be registered after the client
 * has initialized.</p>
 */
public final class XaeroWorldMapBridge {
    private XaeroWorldMapBridge() {}

    /**
     * Registers a layer above the map texture. Lower order values render first.
     */
    public static OverlayRegistration registerMapOverlay(
            String id, int order, MapOverlayRenderer renderer) {
        return OverlayRegistry.registerMap(id, order, Objects.requireNonNull(renderer, "renderer"));
    }

    /**
     * Registers a layer above Xaero's map UI. Lower order values render first.
     */
    public static OverlayRegistration registerUiOverlay(
            String id, int order, UiOverlayRenderer renderer) {
        return OverlayRegistry.registerUi(id, order, Objects.requireNonNull(renderer, "renderer"));
    }
}
