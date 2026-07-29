package io.github.billstark001.xaerobridge.api;

import java.util.Objects;

/**
 * Context for an overlay drawn above Xaero's map texture and below Xaero's map
 * elements and user interface when an exact injection point is available.
 */
public final class MapOverlayContext {
    private final OverlayCanvas canvas;
    private final int width;
    private final int height;
    private final double cameraX;
    private final double cameraZ;
    private final double pixelsPerBlock;
    private final String dimension;

    public MapOverlayContext(OverlayCanvas canvas, int width, int height,
                             double cameraX, double cameraZ,
                             double pixelsPerBlock, String dimension) {
        this.canvas = Objects.requireNonNull(canvas, "canvas");
        this.width = width;
        this.height = height;
        this.cameraX = cameraX;
        this.cameraZ = cameraZ;
        this.pixelsPerBlock = pixelsPerBlock;
        this.dimension = dimension == null ? "unknown" : dimension;
    }

    public OverlayCanvas canvas() { return canvas; }
    public int width() { return width; }
    public int height() { return height; }
    public double cameraX() { return cameraX; }
    public double cameraZ() { return cameraZ; }
    public double pixelsPerBlock() { return pixelsPerBlock; }
    public String dimension() { return dimension; }

    public int worldToScreenX(double worldX) {
        return (int) Math.round(width / 2.0D + (worldX - cameraX) * pixelsPerBlock);
    }

    public int worldToScreenY(double worldZ) {
        return (int) Math.round(height / 2.0D + (worldZ - cameraZ) * pixelsPerBlock);
    }
}
