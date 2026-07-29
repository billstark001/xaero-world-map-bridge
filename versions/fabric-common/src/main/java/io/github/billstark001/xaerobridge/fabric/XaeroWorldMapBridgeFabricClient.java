package io.github.billstark001.xaerobridge.fabric;

import io.github.billstark001.xaerobridge.internal.BridgeSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Initializes the Fabric client-side settings store. */
public final class XaeroWorldMapBridgeFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BridgeSettings.initialize(FabricLoader.getInstance().getConfigDir()
                .resolve("xaero-world-map-bridge.properties"));
    }
}
