package hack.raze.client.mixin.render.screeneffectrenderer;

//? if <=26.1.2 {
import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventRenderFireOverlay;
import hack.raze.client.event.impl.EventRenderFloatingItem;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class MixinScreenEffectRenderer_1_21_11 {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void raze$renderFireOverlay(PoseStack matrices, MultiBufferSource vertexConsumers, TextureAtlasSprite sprite, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderFireOverlay event = new EventRenderFireOverlay();
        event.call();

        if (event.cancelled) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemActivationAnimation", at = @At("HEAD"), cancellable = true)
    public void raze$renderFloatingItem(PoseStack matrices, float tickProgress, SubmitNodeCollector queue, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventRenderFloatingItem event = new EventRenderFloatingItem();
        event.call();

        if (event.cancelled) {
            ci.cancel();
        }
    }
}
//?} else {
/*public final class MixinScreenEffectRenderer_1_21_11 {
}
*///?}
