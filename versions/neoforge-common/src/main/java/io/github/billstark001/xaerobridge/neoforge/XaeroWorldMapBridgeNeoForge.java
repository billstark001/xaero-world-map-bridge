package io.github.billstark001.xaerobridge.neoforge;

import io.github.billstark001.xaerobridge.internal.BridgeSettings;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

/** NeoForge bootstrap. NeoForge has no Mod Menu equivalent dependency. */
@Mod("xaero_world_map_bridge")
public final class XaeroWorldMapBridgeNeoForge {
    public XaeroWorldMapBridgeNeoForge() {
        BridgeSettings.initialize(FMLPaths.CONFIGDIR.get().resolve("xaero-world-map-bridge.properties"));
    }
}
