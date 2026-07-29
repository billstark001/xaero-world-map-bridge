package io.github.billstark001.xaerobridge.mixin;

import io.github.billstark001.xaerobridge.platform.BridgeRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Exact-before-elements and tail-fallback hooks for every published Xaero 1.41--1.44 release on Minecraft 26.2. */
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public abstract class XaeroGuiMapMixin {
    @Unique
    private void xaeroBridge$exact(GuiGraphicsExtractor graphics) {
        Screen screen = (Screen) (Object) this;
        BridgeRenderer.renderExact(screen, graphics, screen.width, screen.height);
    }

    @Unique
    private void xaeroBridge$tail(GuiGraphicsExtractor graphics) {
        Screen screen = (Screen) (Object) this;
        BridgeRenderer.renderTail(screen, graphics, screen.width, screen.height);
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), remap = false, require = 0)
    private void xaeroBridge$begin(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float delta, CallbackInfo callback) { BridgeRenderer.beginPass(); }

    @Inject(method = "extractRenderState", at = @At(
            value = "INVOKE",
            target = "Lxaero/map/element/MapElementRenderHandler;render(Lxaero/map/gui/GuiMap;Lxaero/lib/client/graphics/XaeroBufferProvider;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;",
            shift = At.Shift.BEFORE
    ), remap = false, require = 0, expect = 1)
    private void xaeroBridge$mapLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                      float delta, CallbackInfo callback) { xaeroBridge$exact(graphics); }

    @Inject(method = "extractRenderState", at = @At("TAIL"), remap = false, require = 0)
    private void xaeroBridge$tailLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float delta, CallbackInfo callback) { xaeroBridge$tail(graphics); }
}
