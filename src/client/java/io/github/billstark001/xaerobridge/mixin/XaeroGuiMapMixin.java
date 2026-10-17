package io.github.billstark001.xaerobridge.mixin;

import io.github.billstark001.xaerobridge.platform.BridgeRenderer;
#if MC_VERSION >= 260102
import net.minecraft.client.gui.GuiGraphicsExtractor;
#else
import net.minecraft.client.gui.GuiGraphics;
#endif
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
 * <p>Descriptors match the published Xaero artifacts for each supported target.</p>
 */
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public abstract class XaeroGuiMapMixin {
    @Unique
#if MC_VERSION >= 260102
    private void xaeroBridge$exact(GuiGraphicsExtractor graphics) {
#else
    private void xaeroBridge$exact(GuiGraphics graphics) {
#endif
        Screen screen = (Screen) (Object) this;
        BridgeRenderer.renderExact(screen, graphics, screen.width, screen.height);
    }

    @Unique
#if MC_VERSION >= 260102
    private void xaeroBridge$tail(GuiGraphicsExtractor graphics) {
#else
    private void xaeroBridge$tail(GuiGraphics graphics) {
#endif
        Screen screen = (Screen) (Object) this;
        BridgeRenderer.renderTail(screen, graphics, screen.width, screen.height);
    }

    @Inject(
#if MC_VERSION >= 260102
            method = "extractRenderState",
#else
            method = {"render", "method_25394"},
#endif
            at = @At(value = "HEAD", remap = false),
            remap = false,
            require = 0,
            expect = 1
    )
#if MC_VERSION >= 260102
    private void xaeroBridge$begin(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
#else
    private void xaeroBridge$begin(GuiGraphics graphics, int mouseX, int mouseY,
#endif
                                   float delta, CallbackInfo callback) {
        BridgeRenderer.beginPass();
    }

    @Inject(
#if MC_VERSION >= 260102
            method = "extractRenderState",
#else
            method = {"render", "method_25394"},
#endif
            at = @At(
            value = "INVOKE",
#if MC_VERSION >= 260200
            target = "Lxaero/map/element/MapElementRenderHandler;render(Lxaero/map/gui/GuiMap;Lxaero/lib/client/graphics/XaeroBufferProvider;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;",
#else
            target = "Lxaero/map/element/MapElementRenderHandler;render(Lxaero/map/gui/GuiMap;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lxaero/map/graphics/renderer/multitexture/MultiTextureRenderTypeRendererProvider;DDIIDDDDDFZLxaero/map/element/HoveredMapElementHolder;Lnet/minecraft/client/Minecraft;F)Lxaero/map/element/HoveredMapElementHolder;",
#endif
            shift = At.Shift.BEFORE,
            remap = false
    ), remap = false, require = 0, expect = 1)
#if MC_VERSION >= 260102
    private void xaeroBridge$mapLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
#else
    private void xaeroBridge$mapLayer(GuiGraphics graphics, int mouseX, int mouseY,
#endif
                                      float delta, CallbackInfo callback) {
        xaeroBridge$exact(graphics);
    }

    @Inject(
#if MC_VERSION >= 260102
            method = "extractRenderState",
#else
            method = {"render", "method_25394"},
#endif
            at = @At(value = "TAIL", remap = false),
            remap = false,
            require = 0,
            expect = 1
    )
#if MC_VERSION >= 260102
    private void xaeroBridge$tailLayer(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
#else
    private void xaeroBridge$tailLayer(GuiGraphics graphics, int mouseX, int mouseY,
#endif
                                       float delta, CallbackInfo callback) {
        xaeroBridge$tail(graphics);
    }
}
