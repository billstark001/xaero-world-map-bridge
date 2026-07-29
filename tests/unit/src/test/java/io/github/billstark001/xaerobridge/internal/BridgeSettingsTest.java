package io.github.billstark001.xaerobridge.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgeSettingsTest {
    @TempDir
    Path tempDirectory;

    @AfterEach
    void restoreDefaults() {
        BridgeSettings.initialize(null);
    }

    @Test
    void missingConfigurationRestoresDefaults() {
        BridgeSettings.setMapOverlayEnabled(false);
        BridgeSettings.setUiOverlayEnabled(false);
        BridgeSettings.setInjectionMode(BridgeSettings.InjectionMode.TAIL_FALLBACK);

        BridgeSettings.initialize(tempDirectory.resolve("missing.properties"));

        assertTrue(BridgeSettings.isMapOverlayEnabled());
        assertTrue(BridgeSettings.isUiOverlayEnabled());
        assertEquals(BridgeSettings.InjectionMode.EXACT_ONLY, BridgeSettings.injectionMode());
    }

    @Test
    void savesAndLoadsAllValues() throws IOException {
        Path config = tempDirectory.resolve("nested").resolve("bridge.properties");
        BridgeSettings.initialize(config);
        BridgeSettings.setMapOverlayEnabled(false);
        BridgeSettings.setUiOverlayEnabled(false);
        BridgeSettings.setInjectionMode(BridgeSettings.InjectionMode.TAIL_FALLBACK);
        BridgeSettings.save();

        BridgeSettings.initialize(null);
        BridgeSettings.initialize(config);

        assertFalse(BridgeSettings.isMapOverlayEnabled());
        assertFalse(BridgeSettings.isUiOverlayEnabled());
        assertEquals(BridgeSettings.InjectionMode.TAIL_FALLBACK, BridgeSettings.injectionMode());
        assertTrue(Files.isRegularFile(config));
    }

    @Test
    void malformedConfigurationIsRejectedAtomically() throws IOException {
        Path config = tempDirectory.resolve("invalid.properties");
        Files.writeString(config, """
                mapOverlayEnabled=false
                uiOverlayEnabled=false
                injectionMode=NOT_A_MODE
                """);

        BridgeSettings.initialize(config);

        assertTrue(BridgeSettings.isMapOverlayEnabled());
        assertTrue(BridgeSettings.isUiOverlayEnabled());
        assertEquals(BridgeSettings.InjectionMode.EXACT_ONLY, BridgeSettings.injectionMode());
    }

    @Test
    void malformedBooleanIsNotSilentlyTreatedAsFalse() throws IOException {
        Path config = tempDirectory.resolve("invalid-boolean.properties");
        Files.writeString(config, "mapOverlayEnabled=maybe");

        BridgeSettings.initialize(config);

        assertTrue(BridgeSettings.isMapOverlayEnabled());
        assertTrue(BridgeSettings.isUiOverlayEnabled());
    }

    @Test
    void injectionModeCannotBeNull() {
        assertThrows(NullPointerException.class, () -> BridgeSettings.setInjectionMode(null));
    }
}
