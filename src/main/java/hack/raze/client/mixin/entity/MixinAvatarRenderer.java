package hack.raze.client.mixin.entity;

import hack.raze.client.Raze;
import hack.raze.client.handlers.RotationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer {

    // Third-person silent aim: head looks at the sent rotation, body follows it
    // (vanilla head-turn dynamics) instead of rotating as one unit
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void raze$applyServerHeadRotation(Avatar player, AvatarRenderState state, float tickProgress, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || player == null) return;
        if (player != mc.player) return;
        CameraType perspective = mc.options.getCameraType();
        if (perspective.isFirstPerson()) return;
        if (!RotationHandler.isHasSilentRotation()) return;

        float headRot = RotationHandler.getRenderYaw();
        float bodyRot = RotationHandler.getRenderBodyYaw();

        state.bodyRot = bodyRot;
        state.yRot = Mth.wrapDegrees(headRot - bodyRot);
        state.xRot = Mth.clamp(RotationHandler.getRenderPitch(), -90.0f, 90.0f);
    }



}

