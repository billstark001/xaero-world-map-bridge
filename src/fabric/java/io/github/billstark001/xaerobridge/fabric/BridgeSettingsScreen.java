package io.github.billstark001.xaerobridge.fabric;

import io.github.billstark001.xaerobridge.internal.BridgeSettings;
import io.github.billstark001.xaerobridge.platform.BridgeScreenAccess;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A deliberately small Mod Menu screen with no additional UI dependency. */
final class BridgeSettingsScreen extends Screen {
    private final Screen parent;
    private Button mapButton;
    private Button uiButton;
    private Button modeButton;

    BridgeSettingsScreen(Screen parent) {
        super(Component.literal("Xaero World Map Bridge"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 105;
        int y = this.height / 2 - 34;
        mapButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            BridgeSettings.setMapOverlayEnabled(!BridgeSettings.isMapOverlayEnabled());
            refresh();
        }).bounds(x, y, 210, 20).build());
        uiButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            BridgeSettings.setUiOverlayEnabled(!BridgeSettings.isUiOverlayEnabled());
            refresh();
        }).bounds(x, y + 24, 210, 20).build());
        modeButton = addRenderableWidget(Button.builder(Component.empty(), button -> {
            BridgeSettings.setInjectionMode(BridgeSettings.isTailFallbackEnabled()
                    ? BridgeSettings.InjectionMode.EXACT_ONLY
                    : BridgeSettings.InjectionMode.TAIL_FALLBACK);
            refresh();
        }).bounds(x, y + 48, 210, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(x, y + 82, 210, 20).build());
        refresh();
    }

    @Override
    public void onClose() {
        BridgeSettings.save();
        BridgeScreenAccess.set(parent);
    }

    private void refresh() {
        mapButton.setMessage(Component.literal("Map overlay: "
                + (BridgeSettings.isMapOverlayEnabled() ? "Enabled" : "Disabled")));
        uiButton.setMessage(Component.literal("UI overlay: "
                + (BridgeSettings.isUiOverlayEnabled() ? "Enabled" : "Disabled")));
        modeButton.setMessage(Component.literal("Map injection: "
                + (BridgeSettings.isTailFallbackEnabled() ? "Tail fallback" : "Exact only")));
    }
}
