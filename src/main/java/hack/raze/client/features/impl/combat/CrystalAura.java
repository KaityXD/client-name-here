package hack.raze.client.features.impl.combat;

import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventSystemUpdate;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.FloatSetting;
import hack.raze.client.features.settings.impl.IntSetting;
import hack.raze.client.utils.combat.TargetUtils;
import hack.raze.client.utils.inventory.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Crystal placement and breaking helper for Raze. */
public final class CrystalAura extends Feature {
    private final FloatSetting range = new FloatSetting("Range", 4.0F, 1.0F, 6.0F, 0.1F);
    private final IntSetting actionDelay = new IntSetting("Action Delay", 2, 0, 10, " ticks");
    private int nextActionTick;

    public CrystalAura() {
        super(new FeatureInfo("CrystalAura", "Places and breaks end crystals near the selected target", Category.COMBAT));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        nextActionTick = 0;
    }

    @EventSubscribe
    private void onUpdate(EventSystemUpdate event) {
        if (!event.shouldRunGameTick() || isNull()) return;
        if (mc.player.tickCount < nextActionTick || mc.gameMode == null) return;

        double maxRangeSq = range.getValue() * range.getValue();
        EndCrystal crystal = closestCrystal(maxRangeSq);
        if (crystal != null && InventoryUtils.hasItemInHotbar(item -> item == Items.END_CRYSTAL)) {
            mc.gameMode.attack(mc.player, crystal);
            mc.player.swing(InteractionHand.MAIN_HAND);
            nextActionTick = mc.player.tickCount + actionDelay.getValue();
            return;
        }

        BlockPos base = findBase(maxRangeSq);
        if (base == null) return;
        int slot = InventoryUtils.findItemWithPredicateInHotbar(stack -> stack.is(Items.END_CRYSTAL));
        if (slot == -1) return;

        InventoryUtils.setInvSlot(slot);
        Vec3 hit = Vec3.atCenterOf(base).add(0.0, 0.5, 0.0);
        BlockHitResult result = new BlockHitResult(hit, Direction.UP, base, false);
        if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, result).consumesAction()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
            nextActionTick = mc.player.tickCount + actionDelay.getValue();
        }
    }

    private EndCrystal closestCrystal(double rangeSq) {
        EndCrystal best = null;
        double bestDistance = rangeSq;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal) || crystal.isRemoved()) continue;
            double distance = mc.player.distanceToSqr(crystal);
            if (distance > bestDistance) continue;
            bestDistance = distance;
            best = crystal;
        }
        return best;
    }

    private BlockPos findBase(double rangeSq) {
        var target = TargetUtils.findClosestResolvedTarget(mc.player.getEyePosition(), 1.0F);
        if (target == null) return null;
        BlockPos origin = target.blockPosition();
        BlockPos best = null;
        double bestDistance = rangeSq;
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (!validBase(pos, rangeSq)) continue;
                    double distance = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                    if (distance >= bestDistance) continue;
                    best = pos;
                    bestDistance = distance;
                }
            }
        }
        return best;
    }

    private boolean validBase(BlockPos pos, double rangeSq) {
        if (!mc.level.getBlockState(pos).is(Blocks.OBSIDIAN)
                && !mc.level.getBlockState(pos).is(Blocks.BEDROCK)) return false;
        BlockPos above = pos.above();
        if (!mc.level.isEmptyBlock(above) || !mc.level.isEmptyBlock(above.above())) return false;
        return mc.player.distanceToSqr(Vec3.atCenterOf(pos)) <= rangeSq;
    }
}
