package io.github.billstark001.xaerobridge.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Screen switch adapter shared by all supported Minecraft targets. */
public final class BridgeScreenAccess {
    private BridgeScreenAccess() {}

    public static void set(Screen screen) {
#if MC_VERSION >= 260102
        Minecraft.getInstance().setScreenAndShow(screen);
#else
        Minecraft.getInstance().setScreen(screen);
#endif
    }
}
