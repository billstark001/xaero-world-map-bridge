package io.github.billstark001.xaerobridge.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Provides the three bridge settings through Mod Menu. */
public final class XaeroWorldMapBridgeModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return BridgeSettingsScreen::new;
    }
}
