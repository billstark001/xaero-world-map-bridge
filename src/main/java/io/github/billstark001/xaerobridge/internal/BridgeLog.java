package io.github.billstark001.xaerobridge.internal;

/** Loader-neutral bridge logging backed by the JVM system logger. */
public final class BridgeLog {
    private static final System.Logger LOGGER = System.getLogger("Xaero World Map Bridge");

    private BridgeLog() {}

    public static void warning(String message) {
        LOGGER.log(System.Logger.Level.WARNING, message);
    }

    public static void error(String message, Throwable error) {
        LOGGER.log(System.Logger.Level.ERROR, message, error);
    }
}
