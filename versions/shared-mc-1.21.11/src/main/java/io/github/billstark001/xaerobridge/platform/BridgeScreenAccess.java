package io.github.billstark001.xaerobridge.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Minecraft 1.21.11 screen switch adapter. */
public final class BridgeScreenAccess {
    private BridgeScreenAccess() {}

    public static void set(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }
}
