package hack.raze.client.handlers;

import hack.raze.client.auth.MathProt;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static hack.raze.client.utils.Imports.mc;
/**
 * Yes this is important.
 * Likely not finished.
 *
 * Entity rotation vectors and related hooks.
 *
 * @see MixinEntity#getRotationVec(float) Entity raycasting and targeting
 * @see MixinEntity#getRotationVector() Item interactions
 * @see MixinEntity#getHeadRotationVector() Head-based interactions
 * @see MixinEntity#updateVelocity() Movement input to velocity conversion (MoveFix)
 *
 * @see MixinPlayer#causeExtraKnockback() Attack knockback direction
 * @see MixinPlayer#doSweepAttack() Sweep attack direction
 * @see MixinPlayer#redirectSwimmingLookVector() Swimming travel look vector
 *
 * @see MixinLivingEntity#jump() Sprint jump direction
 * @see MixinLivingEntity#calcGlidingVelocity() Elytra flying pitch and direction
 *
 * @see MixinFireworkRocketEntity#tick() Firework rocket boost direction when elytra flying
 *
 * @see MultiPlayerGameMode#interactItem() Sends PlayerInteractItemC2SPacket with silent yaw/pitch
 * @see MixinItem#raycast() Block interaction range and item raycasting (buckets, pearls, etc.)
 * 
 * @see hack.raze.client.event.impl.EventMovementInput Movement input correction
 */
public class RotationHandler {
    @Getter
    @Setter
    private static float serverYaw = 0f;

    @Getter
    @Setter
    private static float serverPitch = 0f;

    @Getter
    private static float prevServerYaw = 0f;

    @Getter
    private static float prevServerPitch = 0f;

    @Getter
    @Setter
    private static float serverDeltaYaw = 0f;

    @Getter
    @Setter
    private static boolean hasSilentRotation = false;

    // Smoothed render rotations (respect-style: lerps toward the sent rotation so
    // the third person model glides instead of stepping)
    @Getter
    private static float renderYaw;
    @Getter
    private static float renderPitch;
    private static boolean renderInitialized;
    private static long lastRenderNano;

    // Credit to Nami Client for helping me figure this out (the weird AimModulo360
    // flag)

    /**
     * Update server rotations (called every tick to keep them in sync).
     * This prevents the AimModulo360 flag by ensuring continuity.
     */
    public static void updateServerRotations(float yaw, float pitch) {
        // Store previous values for interpolation
        prevServerYaw = serverYaw;
        prevServerPitch = serverPitch;
        // Normalize yaw to maintain continuity relative to previous serverYaw
        serverYaw = serverYaw + Mth.wrapDegrees(yaw - serverYaw);
        float enforcedPitch = MathProt.getEnforcedPitch(pitch);
        serverPitch = Mth.clamp(enforcedPitch, -90f, 90f);
        serverDeltaYaw = 0f;
    }

    /**
     * Set server rotations with delta tracking for silent aim.
     * Calculates the yaw delta from the last server yaw to prevent flags.
     */
    public static void setServerRotationsWithDelta(float yaw, float pitch) {
        // Store previous values for interpolation
        prevServerYaw = serverYaw;
        prevServerPitch = serverPitch;
        // Normalize yaw to maintain continuity relative to previous serverYaw
        float wrappedDelta = Mth.wrapDegrees(yaw - serverYaw);
        serverDeltaYaw = wrappedDelta;
        serverYaw = yaw;
        float enforcedPitch = MathProt.getEnforcedPitch(pitch);
        serverPitch = Mth.clamp(enforcedPitch, -90f, 90f);
        advanceRenderRotations(serverYaw, serverPitch);
    }

    /**
     * Smooth the render-side silent rotation toward the latest sent rotation.
     * Equivalent of respect's 0.4-per-tick render lerp, normalized per frame.
     */
    public static void advanceRenderRotations(float targetYaw, float targetPitch) {
        long n = System.nanoTime();
        if (!renderInitialized) {
            renderYaw = mc.player != null ? mc.player.getYRot() : targetYaw;
            renderPitch = mc.player != null ? mc.player.getXRot() : targetPitch;
            renderInitialized = true;
            lastRenderNano = n;
            return;
        }

        float dt = (n - lastRenderNano) / 1.0E9F;
        lastRenderNano = n;
        if (dt > 0.25F) dt = 0.25F;
        if (dt < 0.001F) dt = 0.001F;

        float f = 1.0F - (float) Math.pow(0.6D, dt / 0.05D);
        renderYaw += Mth.wrapDegrees(targetYaw - renderYaw) * f;
        renderPitch += (targetPitch - renderPitch) * f;
    }

    public static void reset() {
        serverDeltaYaw = 0f;
        hasSilentRotation = false;
        renderInitialized = false;
    }

    public static void syncToPlayer(float playerYaw, float playerPitch) {
        // Store previous values for interpolation
        prevServerYaw = serverYaw;
        prevServerPitch = serverPitch;
        // Normalize yaw to maintain continuity relative to previous serverYaw
        serverYaw = serverYaw + Mth.wrapDegrees(playerYaw - serverYaw);
        float enforcedPitch = MathProt.getEnforcedPitch(playerPitch);
        serverPitch = Mth.clamp(enforcedPitch, -90f, 90f);
        serverDeltaYaw = 0f;
        hasSilentRotation = false;
    }

    public static Vec3 getRotationVector(float pitch, float yaw) {
        float pitchRad = pitch * Mth.DEG_TO_RAD;
        float yawRad = -yaw * Mth.DEG_TO_RAD;
        float cosPitch = Mth.cos(pitchRad);
        float sinPitch = Mth.sin(pitchRad);
        float cosYaw = Mth.cos(yawRad);
        float sinYaw = Mth.sin(yawRad);
        return new Vec3(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }
}
