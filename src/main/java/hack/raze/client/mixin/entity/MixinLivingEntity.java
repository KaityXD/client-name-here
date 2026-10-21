package hack.raze.client.mixin.entity;

import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventShieldBreak;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    @Unique
    private ItemStack raze$previousActiveItem = ItemStack.EMPTY;

    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    // Movefix sprint jump fix (I do have another method too :skull:)
    @ModifyArg(
        method = "jumpFromGround",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private Vec3 adjustSprintJumpVelocity(Vec3 original) {
        return original;
    }

    // Elytra rotation fixes
    @Redirect(
        method = "updateFallFlyingMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getXRot()F"
        )
    )
    private float redirectElytraPitch(LivingEntity instance) {
        return instance.getXRot();
    }

    @Redirect(
        method = "updateFallFlyingMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 redirectElytraRotationVector(LivingEntity instance) {
        return instance.getLookAngle();
    }

    @Inject(method = "onSyncedDataUpdated", at = @At("HEAD"))
    private void onTrackedDataSet(EntityDataAccessor<?> data, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.level().isClientSide()) return;

        if (!(self instanceof Player)) return;

        ItemStack currentActiveItem = self.getUseItem();

        this.raze$previousActiveItem = currentActiveItem.copy();
    }

    // Useless as it only works in singleplayer
    @Inject(method = "onSyncedDataUpdated", at = @At("TAIL"))
    private void onTrackedDataSetTail(EntityDataAccessor<?> data, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.level().isClientSide()) return;

        if (!(self instanceof Player player)) return;

        ItemStack currentActiveItem = self.getUseItem();

        if (!this.raze$previousActiveItem.isEmpty() &&
            this.raze$previousActiveItem.getItem() instanceof ShieldItem &&
            currentActiveItem.isEmpty() &&
            !self.isUsingItem()) {

            EventShieldBreak event = new EventShieldBreak(player);
            event.call();
        }
    }

    @Inject(method = "getCurrentSwingDuration", at = @At("HEAD"), cancellable = true)
    private void Raze$getCurrentSwingDuration(CallbackInfoReturnable<Integer> cir) {
    }
}
