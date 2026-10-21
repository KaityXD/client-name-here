package hack.raze.client.mixin.entity;

import hack.raze.client.Raze;
import hack.raze.client.handlers.RotationHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import static hack.raze.client.utils.Imports.mc;

@Mixin(Player.class)
public class MixinPlayer {

    @Redirect(
            method = "travel",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;"
            )
        )
    private Vec3 redirectSwimmingLookVector(Player entity) {
        return entity.getLookAngle();
    }

    @Redirect(
            method = {"causeExtraKnockback", "doSweepAttack"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"
            )
    )
    private float redirectAttackYaw(Player entity) {
        if (Raze.isDestroyed) return entity.getYRot();
        if (entity == mc.player && RotationHandler.isHasSilentRotation()) {
            return RotationHandler.getServerYaw();
        }
        return entity.getYRot();
    }

    @ModifyVariable(
            method = "causeExtraKnockback",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            ),
            ordinal = 0
    )
    private Vec3 modifyExtraKnockbackVelocity(Vec3 velocity) {
        return velocity;
    }

    @Redirect(
        method = "causeExtraKnockback",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;setSprinting(Z)V"
        )
    )
    private void redirectSetSprinting(Player player, boolean sprinting) {
        player.setSprinting(sprinting);
    }
}
