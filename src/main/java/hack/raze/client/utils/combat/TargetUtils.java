package hack.raze.client.utils.combat;

import hack.raze.client.utils.Imports;
import hack.raze.client.utils.combat.antibot.AntiBotChecks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TargetUtils implements Imports {

    private static LivingEntity keptTarget;
    private static LivingEntity lastAttackedTarget;
    private static int lastAttackedTick;

    private TargetUtils() {
    }

    public static void onAttack(Entity entity) {
        if (mc.player == null) {
            return;
        }

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        lastAttackedTarget = living;
        lastAttackedTick = mc.player.tickCount;

        if (!isTargetAllowed(living)) {
            return;
        }

        keptTarget = living;
    }

    public static boolean isTargetAllowed(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        if (!entity.isAlive() || entity.isRemoved() || entity.isDeadOrDying()) {
            return false;
        }

        if (mc.player == null) {
            return false;
        }

        return true;
    }

    public static LivingEntity resolveTarget(LivingEntity candidate) {
        return candidate;
    }

    public static LivingEntity findClosestResolvedTarget(Vec3 origin, float partialTick) {
        if (origin == null || mc.level == null || mc.player == null) {
            return null;
        }

        LivingEntity bestTarget = null;
        double bestDistSq = Double.MAX_VALUE;

        for (var entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == mc.player) continue;
            if (!isTargetAllowed(living)) continue;

            double distSq = origin.distanceToSqr(living.getPosition(partialTick));
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                bestTarget = living;
            }
        }

        return bestTarget;
    }

    public static void setKeptTarget(LivingEntity target) {
        if (!isKeptTargetValid(target)) {
            return;
        }

        keptTarget = target;
    }

    public static void clearKeptTarget() {
        keptTarget = null;
    }

    public static void clearTrackedTargets() {
        keptTarget = null;
        lastAttackedTarget = null;
        lastAttackedTick = 0;
    }

    public static LivingEntity getLastAttackedTarget(int ticks) {
        if (mc.player == null) {
            return null;
        }

        if (!isLastAttackedStateValid()) {
            return null;
        }

        if (mc.player.tickCount - lastAttackedTick > ticks) {
            return null;
        }

        return lastAttackedTarget;
    }

    public static boolean isBot(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        return AntiBotChecks.isBot(player);
    }

    private static boolean isLastAttackedStateValid() {
        if (lastAttackedTarget == null) {
            return false;
        }

        if (mc.player == null) {
            return false;
        }

        if (lastAttackedTarget.level() != mc.level) {
            return false;
        }

        if (!lastAttackedTarget.isAlive() || lastAttackedTarget.isRemoved()) {
            return false;
        }

        return isTargetAllowed(lastAttackedTarget);
    }

    private static boolean isKeptTargetValid(LivingEntity target) {
        if (target == null || mc.player == null) {
            return false;
        }

        if (!target.isAlive() || target.isRemoved()) {
            return false;
        }

        if (target.level() != mc.level) {
            return false;
        }

        return isTargetAllowed(target);
    }
}
