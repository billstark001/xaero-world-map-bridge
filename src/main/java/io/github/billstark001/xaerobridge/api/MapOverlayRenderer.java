package io.github.billstark001.xaerobridge.api;

/** Renders one layer in map coordinates. */
@FunctionalInterface
public interface MapOverlayRenderer {
    void render(MapOverlayContext context);
}
