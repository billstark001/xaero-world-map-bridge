package io.github.billstark001.xaerobridge.api;

/**
 * A small, loader-neutral drawing surface supplied for one overlay callback.
 * Coordinates are GUI pixels and colors are ARGB integers.
 */
@FunctionalInterface
public interface OverlayCanvas {
    void fill(int left, int top, int right, int bottom, int argb);
}
