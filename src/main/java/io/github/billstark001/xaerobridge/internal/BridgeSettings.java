package io.github.billstark001.xaerobridge.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Loader-neutral persisted settings used by the renderer and Fabric Mod Menu UI. */
public final class BridgeSettings {
    public enum InjectionMode { EXACT_ONLY, TAIL_FALLBACK }

    private static volatile boolean mapOverlayEnabled = true;
    private static volatile boolean uiOverlayEnabled = true;
    private static volatile InjectionMode injectionMode = InjectionMode.EXACT_ONLY;
    private static volatile Path path;

    private BridgeSettings() {}

    public static synchronized void initialize(Path configPath) {
        path = configPath;
        if (configPath == null || !Files.isRegularFile(configPath)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
            mapOverlayEnabled = Boolean.parseBoolean(properties.getProperty("mapOverlayEnabled", "true"));
            uiOverlayEnabled = Boolean.parseBoolean(properties.getProperty("uiOverlayEnabled", "true"));
            injectionMode = InjectionMode.valueOf(properties.getProperty("injectionMode", "EXACT_ONLY"));
        } catch (IOException | IllegalArgumentException error) {
            System.err.println("[Xaero World Map Bridge] Failed to load settings: " + error);
        }
    }

    public static synchronized void save() {
        if (path == null) return;
        Properties properties = new Properties();
        properties.setProperty("mapOverlayEnabled", Boolean.toString(mapOverlayEnabled));
        properties.setProperty("uiOverlayEnabled", Boolean.toString(uiOverlayEnabled));
        properties.setProperty("injectionMode", injectionMode.name());
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "Xaero World Map Bridge settings");
            }
        } catch (IOException error) {
            System.err.println("[Xaero World Map Bridge] Failed to save settings: " + error);
        }
    }

    public static boolean isMapOverlayEnabled() { return mapOverlayEnabled; }
    public static boolean isUiOverlayEnabled() { return uiOverlayEnabled; }
    public static boolean isTailFallbackEnabled() { return injectionMode == InjectionMode.TAIL_FALLBACK; }
    public static InjectionMode injectionMode() { return injectionMode; }
    public static void setMapOverlayEnabled(boolean value) { mapOverlayEnabled = value; }
    public static void setUiOverlayEnabled(boolean value) { uiOverlayEnabled = value; }
    public static void setInjectionMode(InjectionMode value) { injectionMode = value; }
}
