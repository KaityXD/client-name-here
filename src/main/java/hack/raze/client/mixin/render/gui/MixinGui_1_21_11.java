package hack.raze.client.mixin.render.gui;

//? if <26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventRender2DGui;
import hack.raze.client.event.impl.EventRenderEffects;
import hack.raze.client.event.impl.EventRenderNauseaOverlay;
import hack.raze.client.event.impl.EventRenderPortalOverlay;
import hack.raze.client.event.impl.EventRenderScoreboardSidebar;
import hack.raze.client.event.impl.EventRenderSpyglassOverlay;
import hack.raze.client.event.impl.EventRenderVignette;
import hack.raze.client.mixininterface.IGameRenderer;
import hack.raze.client.api.GuiGraphicsCompat;
import hack.raze.client.render2.api.CrossTexture;
import hack.raze.client.render2.impl.opengl.utils.RenderUtil;
import hack.raze.client.utils.VulkanUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hack.raze.client.utils.Imports.mc;

@Mixin(Gui.class)
public class MixinGui_1_21_11 {

    @Inject(method = "render", at = @At("TAIL"))
    private void raze$renderGui2D(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        if (mc.level == null) {
            return;
        }

        if (!VulkanUtil.isVulkanLoaded()) {
            ((IGameRenderer) mc.gameRenderer).raze$flushGuiState();
        }

        var draw = Raze.draw2D;
        draw.beginFrame(RenderUtil.getProjectionMatrix());
        CrossTexture blurTexture = draw.getBlurResult();

        EventRender2DGui event = new EventRender2DGui(
                new PoseStack(),
                deltaTracker.getGameTimeDeltaPartialTick(true),
                GuiGraphicsCompat.of(guiGraphics),
                draw,
                blurTexture
        );
        event.call();
        draw.endFrame();
    }

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    public void raze$renderVignette(GuiGraphics context, Entity entity, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderVignette event = new EventRenderVignette();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "renderConfusionOverlay", at = @At("HEAD"), cancellable = true)
    public void raze$renderNauseaOverlay(GuiGraphics context, float nauseaStrength, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderNauseaOverlay event = new EventRenderNauseaOverlay();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    public void raze$renderSpyglassOverlay(GuiGraphics context, float scale, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderSpyglassOverlay event = new EventRenderSpyglassOverlay();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void raze$renderPortalOverlay(GuiGraphics context, float nauseaStrength, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderPortalOverlay event = new EventRenderPortalOverlay();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "renderEffects", at = @At("STORE"), ordinal = 3)
    private int modifyStatusEffectY(int y) {
        if (Raze.isDestroyed) return y;

        return y;
    }

    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    private void raze$renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderEffects event = new EventRenderEffects();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void raze$renderScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderScoreboardSidebar.Pre event = new EventRenderScoreboardSidebar.Pre(
                GuiGraphicsCompat.of(guiGraphics),
                deltaTracker
        );
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }
}
//?} else {
/*public final class MixinGui_1_21_11 {
}
*///?}
