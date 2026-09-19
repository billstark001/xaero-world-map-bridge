package io.github.billstark001.xaerobridge.api;

/**
 * A small, loader-neutral drawing surface supplied for one overlay callback.
 * Coordinates use Xaero's screen convention: the origin is the top-left GUI
 * pixel, X grows to the right, and Y grows down. Colors are ARGB integers.
 * Use Xaero's native map-element API for text, textures, tooltips and input.
 */
@FunctionalInterface
public interface OverlayCanvas {
    void fill(int left, int top, int right, int bottom, int argb);
}
