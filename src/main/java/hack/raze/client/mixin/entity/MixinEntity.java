package hack.raze.client.mixin.entity;

import hack.raze.client.handlers.RotationHandler;
import hack.raze.client.utils.player.PlayerUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static hack.raze.client.utils.Imports.mc;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    public abstract Vec3 calculateViewVector(float pitch, float yaw);


    @Inject(method = "getViewVector", at = @At("HEAD"), cancellable = true)
    private void injectSilentRotation(float tickDelta, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this == mc.player) {
            if (RotationHandler.isHasSilentRotation()) {
                cir.setReturnValue(calculateViewVector(RotationHandler.getServerPitch(), RotationHandler.getServerYaw()));
            }
        }
    }

    @Inject(method = "getHeadLookAngle", at = @At("HEAD"), cancellable = true)
    private void injectSilentHeadRotation(CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this == mc.player) {
            if (RotationHandler.isHasSilentRotation()) {
                cir.setReturnValue(calculateViewVector(RotationHandler.getServerPitch(), RotationHandler.getServerYaw()));
            }
        }
    }

    @Redirect(
        method = "moveRelative",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getYRot()F"
        )
    )
    private float redirectMovementYaw(Entity instance) {
        return instance.getYRot();
    }

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void raze$onTurn(double yaw, double pitch, CallbackInfo ci) {
    }

}
