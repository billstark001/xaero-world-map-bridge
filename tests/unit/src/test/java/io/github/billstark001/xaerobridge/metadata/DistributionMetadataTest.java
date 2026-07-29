package io.github.billstark001.xaerobridge.metadata;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistributionMetadataTest {
    private final Path projectRoot = Path.of(System.getProperty("xaeroBridge.projectRoot"));

    @Test
    void minecraftTargetsUseExactVersionPredicates() throws IOException {
        String build = Files.readString(projectRoot.resolve("build.gradle"));

        assertFalse(build.contains("minecraftRange        : \"~"));
        assertTrue(build.contains("minecraftRange        : \"1.21.11\""));
        assertTrue(build.contains("minecraftRange        : \"26.1.2\""));
        assertTrue(build.contains("minecraftRange        : \"26.2\""));
    }

    @Test
    void modMenuIsOptional() throws IOException {
        String metadata = Files.readString(projectRoot.resolve("src/main/resources/fabric.mod.json"));
        int depends = metadata.indexOf("\"depends\"");
        int suggests = metadata.indexOf("\"suggests\"");

        assertTrue(depends >= 0 && suggests > depends);
        assertFalse(metadata.substring(depends, suggests).contains("\"modmenu\""));
        assertTrue(metadata.substring(suggests).contains("\"modmenu\""));
    }

    @Test
    void criticalMixinHooksAreRequired() throws IOException {
        String mixin = Files.readString(projectRoot.resolve(
                "versions/shared-mc-1.21.11/src/main/java/io/github/billstark001/xaerobridge/mixin/XaeroGuiMapMixin.java"));
        String normalizedMixin = mixin.replaceAll("\\s+", " ");

        assertTrue(normalizedMixin.contains(
                "at = @At(value = \"HEAD\", remap = false), remap = false, require = 1"));
        assertTrue(normalizedMixin.contains(
                "at = @At(value = \"TAIL\", remap = false), remap = false, require = 1"));
        assertTrue(normalizedMixin.contains("require = 0, expect = 1"));
    }
}
