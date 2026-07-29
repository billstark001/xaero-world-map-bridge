package io.github.billstark001.xaerobridge.api;

/** Renders one layer in screen coordinates above Xaero's UI. */
@FunctionalInterface
public interface UiOverlayRenderer {
    void render(UiOverlayContext context);
}
