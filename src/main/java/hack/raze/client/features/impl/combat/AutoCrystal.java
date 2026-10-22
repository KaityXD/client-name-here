package hack.raze.client.features.impl.combat;

import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventSystemUpdate;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.BoolSetting;
import hack.raze.client.features.settings.impl.FloatSetting;
import hack.raze.client.features.settings.impl.IntSetting;
import hack.raze.client.utils.combat.ExplosionUtils;
import hack.raze.client.utils.inventory.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Automatically places and detonates end crystals around nearby players. */
public final class AutoCrystal extends Feature {
    private final BoolSetting place = new BoolSetting("Place", true);
    private final BoolSetting breakCrystals = new BoolSetting("Break", true);
    private final FloatSetting placeRange = new FloatSetting("Place Range", 4.5F, 1.0F, 6.0F, 0.1F);
    private final FloatSetting breakRange = new FloatSetting("Break Range", 4.5F, 1.0F, 6.0F, 0.1F);
    private final FloatSetting targetRange = new FloatSetting("Target Range", 12.0F, 2.0F, 16.0F, 0.5F);
    private final FloatSetting minimumDamage = new FloatSetting("Minimum Damage", 0.0F, 0.0F, 36.0F, 0.5F);
    private final FloatSetting maximumSelfDamage = new FloatSetting("Maximum Self Damage", 36.0F, 0.0F, 36.0F, 0.5F);
    private final IntSetting actionDelay = new IntSetting("Action Delay", 1, 0, 10, " ticks");

    private int nextActionTick;

    public AutoCrystal() {
        super(new FeatureInfo("AutoCrystal", "Places and breaks crystals using target and self-damage checks", Category.COMBAT));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        nextActionTick = 0;
    }

    @EventSubscribe
    private void onUpdate(EventSystemUpdate event) {
        if (!event.shouldRunGameTick() || isNull() || mc.gameMode == null) return;
        if (mc.player.tickCount < nextActionTick || mc.isPaused()) return;

        Player target = findTarget();

        double breakRangeSq = (double) breakRange.getValue() * breakRange.getValue();
        EndCrystal crystal = breakCrystals.getValue() ? findSafeCrystal(target, breakRangeSq) : null;
        if (crystal != null) {
            mc.gameMode.attack(mc.player, crystal);
            mc.player.swing(InteractionHand.MAIN_HAND);
            scheduleNextAction();
            return;
        }

        if (!place.getValue() || target == null || !InventoryUtils.hasItemInHotbar(item -> item == Items.END_CRYSTAL)) return;
        double placeRangeSq = (double) placeRange.getValue() * placeRange.getValue();
        BlockPos base = findBestBase(target, placeRangeSq);
        if (base == null) return;

        int crystalSlot = InventoryUtils.findItemWithPredicateInHotbar(stack -> stack.is(Items.END_CRYSTAL));
        if (crystalSlot < 0) return;
        InventoryUtils.setInvSlot(crystalSlot);

        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(base).add(0.0, 0.5, 0.0), Direction.UP, base, false);
        if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit).consumesAction()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
            scheduleNextAction();
        }
    }

    private Player findTarget() {
        Player best = null;
        double bestDistance = (double) targetRange.getValue() * targetRange.getValue();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof Player player) || player == mc.player || !player.isAlive() || player.isRemoved()) continue;
            double distance = mc.player.distanceToSqr(player);
            if (distance >= bestDistance) continue;
            bestDistance = distance;
            best = player;
        }
        return best;
    }

    private EndCrystal findSafeCrystal(Player target, double rangeSq) {
        EndCrystal best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal) || crystal.isRemoved()) continue;
            if (mc.player.distanceToSqr(crystal) > rangeSq) continue;
            Vec3 center = crystal.position();
            float targetDamage = minimumDamage.getValue() > 0.0F && target != null
                    ? ExplosionUtils.getExplosionDamageTo(target, center, 6.0F) : 0.0F;
            float selfDamage = maximumSelfDamage.getValue() < 36.0F
                    ? ExplosionUtils.getExplosionDamageTo(mc.player, center, 6.0F) : 0.0F;
            if (minimumDamage.getValue() > 0.0F && (target == null || targetDamage < minimumDamage.getValue())) continue;
            if (selfDamage > maximumSelfDamage.getValue()) continue;
            double score = targetDamage - selfDamage * 0.25 - mc.player.distanceToSqr(crystal) * 0.01;
            if (score > bestScore) {
                bestScore = score;
                best = crystal;
            }
        }
        return best;
    }

    private BlockPos findBestBase(Player target, double rangeSq) {
        BlockPos origin = target.blockPosition();
        BlockPos best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (!validBase(pos, rangeSq)) continue;

                    Vec3 center = Vec3.atCenterOf(pos.above());
                    float targetDamage = minimumDamage.getValue() > 0.0F
                            ? ExplosionUtils.getExplosionDamageTo(target, center, 6.0F) : 0.0F;
                    if (minimumDamage.getValue() > 0.0F && targetDamage < minimumDamage.getValue()) continue;
                    float selfDamage = maximumSelfDamage.getValue() < 36.0F
                            ? ExplosionUtils.getExplosionDamageTo(mc.player, center, 6.0F) : 0.0F;
                    if (selfDamage > maximumSelfDamage.getValue()) continue;

                    double score = minimumDamage.getValue() > 0.0F
                            ? targetDamage - selfDamage * 0.25 - mc.player.distanceToSqr(center) * 0.01
                            : -target.distanceToSqr(center) - mc.player.distanceToSqr(center) * 0.1;
                    if (score > bestScore) {
                        bestScore = score;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    private boolean validBase(BlockPos pos, double rangeSq) {
        if (!mc.level.getBlockState(pos).is(Blocks.OBSIDIAN)
                && !mc.level.getBlockState(pos).is(Blocks.BEDROCK)) return false;
        BlockPos crystalPos = pos.above();
        if (!mc.level.isEmptyBlock(crystalPos) || !mc.level.isEmptyBlock(crystalPos.above())) return false;
        if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > rangeSq) return false;

        AABB crystalSpace = new AABB(crystalPos.getX(), crystalPos.getY(), crystalPos.getZ(),
                crystalPos.getX() + 1.0, crystalPos.getY() + 2.0, crystalPos.getZ() + 1.0);
        return mc.level.getEntities(null, crystalSpace).isEmpty();
    }

    private void scheduleNextAction() {
        nextActionTick = mc.player.tickCount + actionDelay.getValue();
    }
}
