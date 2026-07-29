package io.github.billstark001.xaerobridge.api;

/** Removes a registered overlay. Calling {@link #close()} more than once is safe. */
@FunctionalInterface
public interface OverlayRegistration extends AutoCloseable {
    @Override
    void close();
}
