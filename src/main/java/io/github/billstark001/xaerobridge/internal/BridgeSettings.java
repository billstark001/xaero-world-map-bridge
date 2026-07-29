package io.github.billstark001.xaerobridge.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
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
        boolean nextMapOverlayEnabled = true;
        boolean nextUiOverlayEnabled = true;
        InjectionMode nextInjectionMode = InjectionMode.EXACT_ONLY;
        if (configPath == null || !Files.isRegularFile(configPath)) {
            apply(nextMapOverlayEnabled, nextUiOverlayEnabled, nextInjectionMode);
            return;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
            nextMapOverlayEnabled = parseBoolean(properties, "mapOverlayEnabled", true);
            nextUiOverlayEnabled = parseBoolean(properties, "uiOverlayEnabled", true);
            nextInjectionMode = InjectionMode.valueOf(properties.getProperty("injectionMode", "EXACT_ONLY"));
        } catch (IOException | IllegalArgumentException error) {
            BridgeLog.error("Failed to load settings from " + configPath, error);
            apply(true, true, InjectionMode.EXACT_ONLY);
            return;
        }
        apply(nextMapOverlayEnabled, nextUiOverlayEnabled, nextInjectionMode);
    }

    public static synchronized void save() {
        if (path == null) return;
        Properties properties = new Properties();
        properties.setProperty("mapOverlayEnabled", Boolean.toString(mapOverlayEnabled));
        properties.setProperty("uiOverlayEnabled", Boolean.toString(uiOverlayEnabled));
        properties.setProperty("injectionMode", injectionMode.name());
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, "Xaero World Map Bridge settings");
            }
        } catch (IOException error) {
            BridgeLog.error("Failed to save settings to " + path, error);
        }
    }

    private static boolean parseBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new IllegalArgumentException("Invalid boolean value for " + key + ": " + value);
    }

    private static void apply(boolean mapEnabled, boolean uiEnabled, InjectionMode mode) {
        mapOverlayEnabled = mapEnabled;
        uiOverlayEnabled = uiEnabled;
        injectionMode = mode;
    }

    public static boolean isMapOverlayEnabled() { return mapOverlayEnabled; }
    public static boolean isUiOverlayEnabled() { return uiOverlayEnabled; }
    public static boolean isTailFallbackEnabled() { return injectionMode == InjectionMode.TAIL_FALLBACK; }
    public static InjectionMode injectionMode() { return injectionMode; }
    public static void setMapOverlayEnabled(boolean value) { mapOverlayEnabled = value; }
    public static void setUiOverlayEnabled(boolean value) { uiOverlayEnabled = value; }
    public static void setInjectionMode(InjectionMode value) {
        injectionMode = Objects.requireNonNull(value, "value");
    }
}
