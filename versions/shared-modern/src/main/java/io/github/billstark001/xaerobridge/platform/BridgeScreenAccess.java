package io.github.billstark001.xaerobridge.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Shared screen switch adapter for Minecraft 26.1.2 through 26.3. */
public final class BridgeScreenAccess {
    private BridgeScreenAccess() {}

    public static void set(Screen screen) {
        Minecraft.getInstance().setScreenAndShow(screen);
    }
}
