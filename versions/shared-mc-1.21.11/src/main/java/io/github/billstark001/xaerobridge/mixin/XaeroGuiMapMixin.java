package io.github.billstark001.xaerobridge.mixin;

import io.github.billstark001.xaerobridge.platform.BridgeRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The exact anchor precedes Xaero's element and UI rendering. The tail hook is
 * intentionally only a fallback because it draws above Xaero's UI.
 *
 * <p>The anchor descriptor was verified against every published Fabric and
 * NeoForge Xaero World Map 1.40--1.44 artifact for Minecraft 1.21.11.</p>
 */
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public abstract class XaeroGuiMapMixin {
    @Unique
    private void xaeroBridge$exact(GuiGraphics graphics) {
        Screen screen = (Screen) (Object) this;
        BridgeRenderer.renderExact(screen, graphics, screen.width, screen.height);
    }

    @Unique
    private void xaeroBridge$tail(GuiGraphics graphics) {
        Screen screen = (Screen) (Object) this;
        BridgeRenderer.renderTail(screen, graphics, screen.width, screen.height);
    }

    @Inject(
            method = {"render", "method_25394"},
            at = @At(value = "HEAD", remap = false),
            remap = false,
            require = 1
    )
    private void xaeroBridge$begin(GuiGraphics graphics, int mouseX, int mouseY,
                                   float delta, CallbackInfo callback) {
        BridgeRenderer.beginPass();
    }

    @Inject(method = {"render", "method_25394"}, at = @At(
            value = "INVOKE",
            target = "Lxaero/map/element/MapElementRenderHandler;render(Lxaero/map/gui/GuiMap;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;",
            shift = At.Shift.BEFORE,
            remap = false
    ), remap = false, require = 0, expect = 1)
    private void xaeroBridge$mapLayer(GuiGraphics graphics, int mouseX, int mouseY,
                                      float delta, CallbackInfo callback) {
        xaeroBridge$exact(graphics);
    }

    @Inject(
            method = {"render", "method_25394"},
            at = @At(value = "TAIL", remap = false),
            remap = false,
            require = 1
    )
    private void xaeroBridge$tailLayer(GuiGraphics graphics, int mouseX, int mouseY,
                                       float delta, CallbackInfo callback) {
        xaeroBridge$tail(graphics);
    }
}
