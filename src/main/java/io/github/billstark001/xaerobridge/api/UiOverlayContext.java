package io.github.billstark001.xaerobridge.api;

import java.util.Objects;

/**
 * Context for an overlay deliberately drawn above Xaero's complete UI.
 */
public final class UiOverlayContext {
    private final OverlayCanvas canvas;
    private final int width;
    private final int height;

    public UiOverlayContext(OverlayCanvas canvas, int width, int height) {
        this.canvas = Objects.requireNonNull(canvas, "canvas");
        this.width = width;
        this.height = height;
    }

    public OverlayCanvas canvas() { return canvas; }
    public int width() { return width; }
    public int height() { return height; }
}
