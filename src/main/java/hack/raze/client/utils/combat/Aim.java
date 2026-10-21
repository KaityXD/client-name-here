package hack.raze.client.utils.combat;

import hack.raze.client.utils.Imports;
import hack.raze.client.utils.rotation.RotationUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Aim implements Imports {

    private Aim() {}

    public enum Point {
        HEAD,
        CENTER,
        CLOSEST,
        DYNAMIC
    }

    public static Point resolve(LivingEntity target, Point mode, float partialTick) {
        if (mode != Point.DYNAMIC || target == null || mc.player == null) return mode;
        return mc.player.getEyePosition(partialTick).distanceTo(target.getBoundingBox().getCenter()) < 2.4
                ? Point.CENTER
                : Point.HEAD;
    }

    public static Vec3 point(LivingEntity target, Point mode, float partialTick) {
        if (target == null) return null;
        AABB box = RotationUtils.getInterpolatedAABB(target, partialTick);
        Vec3 center = box.getCenter();
        Vec3 eye = mc.player != null ? mc.player.getEyePosition(partialTick) : center;
        Vec3 head = new Vec3(center.x, box.maxY - 0.1, center.z);

        if (mode == Point.DYNAMIC) {
            double d = eye.distanceTo(center);
            if (d >= 2.4) return head;
            double t = Math.max(0.0D, (d - 1.0D) / 1.4D);
            double low = box.minY + (box.maxY - box.minY) * 0.2D;
            return new Vec3(center.x, low + (head.y - low) * t, center.z);
        }

        switch (mode) {
            case HEAD:
                return head;
            case CENTER:
                return center;
            case CLOSEST:
                double cx = Math.min(Math.max(eye.x, box.minX), box.maxX);
                double cy = Math.min(Math.max(eye.y, box.minY), box.maxY);
                double cz = Math.min(Math.max(eye.z, box.minZ), box.maxZ);
                return new Vec3(cx, cy, cz);
            default:
                return center;
        }
    }
}
