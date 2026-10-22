package hack.raze.client.features.impl.combat;

import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventSystemUpdate;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.FloatSetting;
import hack.raze.client.features.settings.impl.IntSetting;
import hack.raze.client.utils.blocks.BlockUtils;
import hack.raze.client.utils.combat.TargetUtils;
import hack.raze.client.utils.inventory.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Respawn anchor placement and charge sequence, implemented with Raze APIs. */
public final class AutoAnchor extends Feature {
    private final FloatSetting range = new FloatSetting("Range", 4.0F, 1.0F, 6.0F, 0.1F);
    private final IntSetting charges = new IntSetting("Charges", 4, 1, 4);
    private final IntSetting actionDelay = new IntSetting("Action Delay", 2, 0, 10, " ticks");

    private BlockPos activeAnchor;
    private int nextActionTick;

    public AutoAnchor() {
        super(new FeatureInfo("AutoAnchor", "Places, charges, and detonates respawn anchors near a target", Category.COMBAT));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }

    @EventSubscribe
    private void onUpdate(EventSystemUpdate event) {
        if (!event.shouldRunGameTick() || isNull() || mc.gameMode == null) return;
        if (mc.level.dimension() == Level.OVERWORLD) return;
        if (mc.player.tickCount < nextActionTick) return;

        if (activeAnchor != null && !isAnchor(activeAnchor)) reset();
        if (activeAnchor == null) {
            activeAnchor = findAnchorSpot();
            if (activeAnchor == null || !placeAnchor(activeAnchor)) return;
            delayNextAction();
            return;
        }

        int charge = mc.level.getBlockState(activeAnchor).getValue(RespawnAnchorBlock.CHARGE);
        if (charge < charges.getValue()) {
            int glowstoneSlot = InventoryUtils.findItemWithPredicateInHotbar(stack -> stack.is(Items.GLOWSTONE));
            if (glowstoneSlot == -1) {
                reset();
                return;
            }
            InventoryUtils.setInvSlot(glowstoneSlot);
            if (interactAnchor(activeAnchor)) {
                delayNextAction();
            }
            return;
        }

        int safeHandSlot = InventoryUtils.findItemWithPredicateInHotbar(stack ->
                !stack.isEmpty() && !stack.is(Items.GLOWSTONE) && !stack.is(Items.RESPAWN_ANCHOR));
        if (safeHandSlot == -1) {
            safeHandSlot = InventoryUtils.findItemWithPredicateInHotbar(stack -> stack.isEmpty());
        }
        if (safeHandSlot != -1) InventoryUtils.setInvSlot(safeHandSlot);
        if (interactAnchor(activeAnchor)) {
            reset();
            delayNextAction();
        }
    }

    private BlockPos findAnchorSpot() {
        LivingEntity target = TargetUtils.findClosestResolvedTarget(mc.player.getEyePosition(), 1.0F);
        if (target == null) return null;

        BlockPos center = target.blockPosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = -1; y <= 1; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!BlockUtils.isValidPlacement(pos) || BlockUtils.collidesWithPlayer(pos)
                            || BlockUtils.collidesWithBlockingEntity(pos)) continue;
                    double targetDistance = Vec3.atCenterOf(pos).distanceToSqr(target.position());
                    double playerDistance = Vec3.atCenterOf(pos).distanceToSqr(mc.player.position());
                    if (playerDistance > range.getValue() * range.getValue()) continue;
                    double score = targetDistance + playerDistance * 0.15;
                    if (score >= bestScore) continue;
                    best = pos;
                    bestScore = score;
                }
            }
        }
        return best;
    }

    private boolean placeAnchor(BlockPos pos) {
        int slot = InventoryUtils.findItemWithPredicateInHotbar(stack -> stack.is(Items.RESPAWN_ANCHOR));
        if (slot == -1) return false;
        BlockUtils.PlacementHit placement = BlockUtils.findPlacementHit(pos, true);
        if (placement == null) return false;
        InventoryUtils.setInvSlot(slot);
        InteractionResult result = BlockUtils.interactWithBlock(placement.hitResult(), true);
        return result.consumesAction();
    }

    private boolean interactAnchor(BlockPos pos) {
        if (!isAnchor(pos)) return false;
        Vec3 point = Vec3.atCenterOf(pos);
        BlockHitResult hit = new BlockHitResult(point, Direction.UP, pos, false);
        return BlockUtils.interactWithBlock(hit, true).consumesAction();
    }

    private boolean isAnchor(BlockPos pos) {
        return pos != null && mc.level.getBlockState(pos).is(Blocks.RESPAWN_ANCHOR);
    }

    private void delayNextAction() {
        nextActionTick = mc.player.tickCount + actionDelay.getValue();
    }

    private void reset() {
        activeAnchor = null;
    }
}
