package hack.raze.client.mixin.render.gui;

//? if ~26.1 {
/*import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventRenderEffects;
import hack.raze.client.event.impl.EventRenderNauseaOverlay;
import hack.raze.client.event.impl.EventRenderPortalOverlay;
import hack.raze.client.event.impl.EventRenderScoreboardSidebar;
import hack.raze.client.event.impl.EventRenderSpyglassOverlay;
import hack.raze.client.event.impl.EventRenderVignette;
import hack.raze.client.api.GuiGraphicsCompat;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui_26_1 {

    @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
    public void raze$renderVignette(GuiGraphicsExtractor context, Entity entity, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderVignette event = new EventRenderVignette();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
    public void raze$renderNauseaOverlay(GuiGraphicsExtractor context, float nauseaStrength, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderNauseaOverlay event = new EventRenderNauseaOverlay();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "extractSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    public void raze$renderSpyglassOverlay(GuiGraphicsExtractor context, float scale, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderSpyglassOverlay event = new EventRenderSpyglassOverlay();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void raze$renderPortalOverlay(GuiGraphicsExtractor context, float nauseaStrength, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderPortalOverlay event = new EventRenderPortalOverlay();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "extractEffects", at = @At("STORE"), ordinal = 3)
    private int modifyStatusEffectY(int y) {
        if (Raze.isDestroyed) return y;

        return y;
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void raze$renderEffects(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderEffects event = new EventRenderEffects();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void raze$renderScoreboardSidebar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
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
*///?} else {
public final class MixinGui_26_1 {
}
//?}
