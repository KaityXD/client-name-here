package hack.raze.client.mixin.client.multiplayergamemode;

//? if >=26.1 {
/*import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventItemUse;
import hack.raze.client.event.impl.EventOnAttackEntity;
import hack.raze.client.event.impl.EventPerformUseItemOn;
import hack.raze.client.handlers.RotationHandler;
import hack.raze.client.utils.combat.TargetUtils;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static hack.raze.client.utils.Imports.mc;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode_26_1 {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private GameType localPlayerMode;
    @Shadow protected abstract void ensureHasSentCarriedItem();
    @Shadow protected abstract void startPrediction(ClientLevel world, PredictiveAction packetCreator);

    @Inject(method = {"handleInventoryMouseClick", "handleContainerInput"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void onClickSlot(
            int syncId,
            int slotId,
            int button,
            ContainerInput actionType,
            Player player,
            CallbackInfo ci
    ) {
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void injectInteractItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (Raze.featureManager != null && !Raze.isDestroyed) {
            EventItemUse.Pre event = new EventItemUse.Pre(player, hand);
            event.call();
            if (event.cancelled) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
        }
        InteractionResult result = this.raze$handleInteractItem(player, hand);
        cir.setReturnValue(result);
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void raze$onUseItemPost(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (Raze.featureManager != null && !Raze.isDestroyed) {
            EventItemUse.Post event = new EventItemUse.Post(player, hand);
            event.call();
        }
    }

    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void raze$onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult bhr, CallbackInfoReturnable<InteractionResult> cir) {
        if (Raze.featureManager != null && !Raze.isDestroyed) {
            EventPerformUseItemOn.Pre event = new EventPerformUseItemOn.Pre(player, hand, bhr);
            event.call();
            if (event.cancelled) {
                cir.setReturnValue(InteractionResult.PASS);
                return;
            }
        }
    }

    @Inject(method = "performUseItemOn", at = @At("RETURN"))
    private void raze$onUseItemOnPost(LocalPlayer player, InteractionHand hand, BlockHitResult bhr, CallbackInfoReturnable<InteractionResult> cir) {
        if (Raze.featureManager != null && !Raze.isDestroyed) {
            EventPerformUseItemOn.Post event = new EventPerformUseItemOn.Post(player, hand, bhr);
            event.call();
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void raze$onAttackPre(Player player, Entity entity, CallbackInfo ci) {
        if (Raze.isDestroyed) return;
        if (Raze.featureManager == null) return;
        EventOnAttackEntity.Pre event = new EventOnAttackEntity.Pre(player, entity);
        event.call();
        if (event.cancelled) ci.cancel();
    }

    @Inject(method = "attack", at = @At("TAIL"))
    private void raze$onAttackPost(Player player, Entity entity, CallbackInfo ci) {
        if (Raze.isDestroyed) return;
        TargetUtils.onAttack(entity);
        if (Raze.featureManager == null) return;
        EventOnAttackEntity.Post event = new EventOnAttackEntity.Post(player, entity);
        event.call();
    }

    @ModifyConstant(method = "continueDestroyBlock", constant = @Constant(intValue = 5))
    public int raze$updateBlockBreakingProgress(int constant) {
        return constant;
    }

    private InteractionResult raze$handleInteractItem(Player player, InteractionHand hand) {
        if (this.localPlayerMode == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        }

        this.ensureHasSentCarriedItem();
        AtomicReference<InteractionResult> resultRef = new AtomicReference<>(InteractionResult.PASS);

        float packetYaw = player.getYRot();
        float packetPitch = player.getXRot();
        if (player == mc.player && RotationHandler.isHasSilentRotation()) {
            packetYaw = RotationHandler.getServerYaw();
            packetPitch = RotationHandler.getServerPitch();
        }

        ClientLevel world = this.minecraft.level;
        if (world == null) {
            return InteractionResult.PASS;
        }

        float finalYaw = packetYaw;
        float finalPitch = packetPitch;
        this.startPrediction(world, sequence -> {
            ServerboundUseItemPacket packet = new ServerboundUseItemPacket(hand, sequence, finalYaw, finalPitch);
            ItemStack stackInHand = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(stackInHand)) {
                resultRef.set(InteractionResult.PASS);
                return packet;
            }

            InteractionResult useResult = stackInHand.use(world, player, hand);
            ItemStack updatedStack;
            if (useResult instanceof InteractionResult.Success success) {
                updatedStack = Objects.requireNonNullElseGet(success.heldItemTransformedTo(), () -> player.getItemInHand(hand));
            } else {
                updatedStack = player.getItemInHand(hand);
            }

            if (updatedStack != stackInHand) {
                player.setItemInHand(hand, updatedStack);
            }

            resultRef.set(useResult);
            return packet;
        });

        return resultRef.get();
    }
}
*///?} else {
public final class MixinMultiPlayerGameMode_26_1 {
}
//?}
