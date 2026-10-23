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

    // View-swap state, set by a feature that replaces the player's actual rotation with
    // the server rotation for part of the tick (e.g. StrafeFix during aiStep). Used by
    // MixinLocalPlayer to shield vanilla view-driven state (yBob/xBob, which drives the
    // first-person hand wobble in ItemInHandRenderer) from the silent rotation.
    @Getter
    @Setter
    private static boolean hasViewSwap = false;
    @Getter
    @Setter
    private static float viewSwapOrigYaw = 0f;
    @Getter
    @Setter
    private static float viewSwapOrigPitch = 0f;

    // Smoothed third-person preview rotations. Advances every frame:
    // the head eases toward the sent rotation while the body follows it with the
    // vanilla LivingEntity.tickHeadTurn dynamics instead of snapping to it.
    @Getter
    private static float renderYaw;
    @Getter
    private static float renderPitch;
    @Getter
    private static float renderBodyYaw;
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
    }

    /**
     * Advance the third-person rotation preview. Called every frame.
     *
     * Head rotation eases toward the last sent rotation. The body chases the head with the
     * vanilla {@code LivingEntity.tickHeadTurn} rules (0.3-per-tick lerp toward the target
     * orientation, velocity direction bias while moving, view yaw snap while swinging and a
     * capped max head offset relative to the body), rescaled from per-tick to per-frame.
     */
    public static void advanceRenderRotations() {
        long n = System.nanoTime();

        if (!isHasSilentRotation()) {
            renderInitialized = false;
            lastRenderNano = n;
            return;
        }

        if (!renderInitialized) {
            if (mc.player != null) {
                float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                renderYaw = mc.player.getYRot();
                renderPitch = mc.player.getXRot();
                renderBodyYaw = Mth.rotLerp(partialTick, mc.player.yBodyRotO, mc.player.yBodyRot);
            } else {
                renderYaw = serverYaw;
                renderPitch = serverPitch;
                renderBodyYaw = serverYaw;
            }
            renderInitialized = true;
            lastRenderNano = n;
            return;
        }

        float dt = (n - lastRenderNano) / 1.0E9F;
        lastRenderNano = n;
        if (dt > 0.25F) dt = 0.25F;
        if (dt <= 1.0E-5F) return;

        // Head: time-normalized ease toward the last sent rotation
        float headEase = 1.0F - (float) Math.pow(0.6D, dt / 0.05D);
        renderYaw += Mth.wrapDegrees(serverYaw - renderYaw) * headEase;
        renderPitch += (serverPitch - renderPitch) * headEase;

        // Body: vanilla body simulation, scaled from per-tick to per-frame
        float deltaTicks = dt / 0.05F;
        float chase = Math.min(0.3F * deltaTicks, 1.0F);
        float bodyTarget = renderYaw;

        if (mc.player != null) {
            // Vanilla: while moving, the body aims at the direction of travel
            Vec3 velocity = mc.player.getDeltaMovement();
            double velDistSq = velocity.x * velocity.x + velocity.z * velocity.z;
            if (velDistSq > 0.0025D) {
                float velYaw = (float) (Math.toDegrees(Math.atan2(velocity.z, velocity.x))) - 90.0F;
                float viewDiff = Math.abs(Mth.wrapDegrees(renderYaw - velYaw));
                bodyTarget = (viewDiff > 95.0F && viewDiff < 265.0F) ? velYaw + 180.0F : velYaw;
            }
            // Vanilla: while swinging, the body faces the view direction
            if (mc.player.attackAnim > 0.0F) {
                bodyTarget = renderYaw;
            }
        }

        renderBodyYaw += Mth.wrapDegrees(bodyTarget - renderBodyYaw) * chase;
        float bodyHeadDiff = Mth.wrapDegrees(renderYaw - renderBodyYaw);
        float maxHeadFromBody = mc.player != null && mc.player.isBlocking() ? 15.0F : 50.0F;
        if (Math.abs(bodyHeadDiff) > maxHeadFromBody) {
            renderBodyYaw += bodyHeadDiff - Mth.sign(bodyHeadDiff) * maxHeadFromBody;
        }
        renderBodyYaw = Mth.wrapDegrees(renderBodyYaw);
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
