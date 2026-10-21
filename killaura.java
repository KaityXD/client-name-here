package com.kindo.ghost.client.modules.impl.combat;

import com.kindo.ghost.client.GhostClient;
import com.kindo.ghost.client.modules.Module;
import com.kindo.ghost.client.settings.BooleanSetting;
import com.kindo.ghost.client.settings.EnumSetting;
import com.kindo.ghost.client.settings.NumberSetting;
import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.hit.HitResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

public class KillAura extends Module {

    // --- Ghost Configuration System ---
    private static final String GHOST_CONFIG_DIR = "config/ghost/auras/";
    private final Gson gson = new Gson();
    private GhostConfig currentConfig;
    private final Map<String, GhostConfig> loadedConfigs = new HashMap<>();

    public static class GhostConfig {
        public String name = "Default";
        public float lerpFactor = 0.35f;
        public float snapAngle = 10.0f;
        public float jitterAmount = 0.0f;
        public int attackCooldown = 0;
        public boolean silentRotation = true;
        public boolean predictiveAim = true;
        public int predictionTicks = 3;
        public float velocityFactor = 0.8f;
        public boolean randomizeAttack = true;
        public float minLerp = 0.1f;
        public float maxLerp = 0.8f;
        public boolean adaptiveLerp = true;
        public boolean bypassHeuristics = true;
        public float critChance = 1.0f;
    }

    // --- Settings ---
    private final EnumSetting profile = new EnumSetting("Profile", "Default", new String[]{"Default", "Grim", "Matrix", "NCP", "Custom"});
    private final NumberSetting range = new NumberSetting("Range", 4.5, 3.0, 6.0, 0.1);
    private final NumberSetting switchDelay = new NumberSetting("SwitchDelay", 50, 0, 500, 10);
    private final BooleanSetting multiTarget = new BooleanSetting("MultiTarget", true);
    private final NumberSetting maxTargets = new NumberSetting("MaxTargets", 3, 1, 8, 1);
    private final BooleanSetting crit = new BooleanSetting("Crit", false);
    private final BooleanSetting autoAim = new BooleanSetting("AutoAim", true);
    private final BooleanSetting onlyInReach = new BooleanSetting("OnlyInReach", true);
    private final BooleanSetting switchItem = new BooleanSetting("SwitchItem", true);
    private final BooleanSetting silent = new BooleanSetting("Silent", true);
    private final BooleanSetting predictiveAim = new BooleanSetting("PredictiveAim", true);
    private final BooleanSetting adaptiveLerp = new BooleanSetting("AdaptiveLerp", true);
    private final BooleanSetting randomizeAttack = new BooleanSetting("RandomizeAttack", true);

    // --- State ---
    private int lastSwitchTick;
    private int lastAttackTick;
    private float silentYaw;
    private float silentPitch;
    private boolean isAttacking;
    private final Random random = new Random();
    private float adaptiveLerpCurrent;
    private long lastBypassTick;
    private boolean bypassActive;

    public KillAura() {
        super("KillAura", "Combat", "Advanced KillAura with Ghost Bypass Profiles.");
        addSetting(profile, range, switchDelay, multiTarget, maxTargets, crit, autoAim, 
                   onlyInReach, switchItem, silent, predictiveAim, adaptiveLerp, randomizeAttack);
        
        // Initialize Default Config
        currentConfig = new GhostConfig();
        loadConfig("Default");
        
        // Load Preset Ghost Configs
        loadPreset("Grim");
        loadPreset("Matrix");
        loadPreset("NCP");
    }

    private void loadPreset(String name) {
        File file = new File(GHOST_CONFIG_DIR + name + ".json");
        if (!file.exists()) {
            // Create default preset if missing
            GhostConfig config = new GhostConfig();
            config.name = name;
            switch (name) {
                case "Grim":
                    config.silentRotation = true;
                    config.lerpFactor = 0.4f;
                    config.jitterAmount = 0.1f;
                    config.attackCooldown = 1;
                    break;
                case "Matrix":
                    config.silentRotation = true;
                    config.lerpFactor = 0.25f;
                    config.snapAngle = 5.0f;
                    config.attackCooldown = 0;
                    break;
                case "NCP":
                    config.silentRotation = true;
                    config.lerpFactor = 0.5f;
                    config.jitterAmount = 0.2f;
                    config.attackCooldown = 2;
                    break;
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(config, writer);
            } catch (Exception e) { e.printStackTrace(); }
        }
        loadConfig(name);
    }

    private void loadConfig(String name) {
        File file = new File(GHOST_CONFIG_DIR + name + ".json");
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                currentConfig = gson.fromJson(reader, GhostConfig.class);
            } catch (Exception e) {
                currentConfig = new GhostConfig();
            }
        }
    }

    public void saveConfig() {
        File file = new File(GHOST_CONFIG_DIR + currentConfig.name + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(currentConfig, writer);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        ClientPlayerEntity player = mc.player;
        if (player.isDead()) return;

        // Update Adaptive Lerp
        if (currentConfig.adaptiveLerp) {
            float t = (System.currentTimeMillis() % 2000) / 2000.0f;
            adaptiveLerpCurrent = currentConfig.minLerp + 
                (currentConfig.maxLerp - currentConfig.minLerp) * (0.5f - Math.cos(t * Math.PI * 2) * 0.5f);
        } else {
            adaptiveLerpCurrent = currentConfig.lerpFactor;
        }

        // Find Targets
        List<LivingEntity> targets = getTargets(player);

        if (targets.isEmpty()) {
            if (autoAim.getValue()) {
                resetSilentRotation(player);
            }
            return;
        }

        // Sort & Limit
        targets.sort((a, b) -> Float.compare(getTargetScore(b, player), getTargetScore(a, player)));
        if (multiTarget.getValue() && targets.size() > maxTargets.getValue().intValue()) {
            targets = targets.subList(0, maxTargets.getValue().intValue());
        }

        // Handle Item Switching
        if (switchItem.getValue()) handleItemSwitch(player);

        // Attack Logic
        int currentTick = mc.world.getTime();
        if (currentTick - lastAttackTick >= currentConfig.attackCooldown) {
            for (LivingEntity target : targets) {
                if (canAttack(target, player)) {
                    attackTarget(player, target);
                    lastAttackTick = currentTick;
                    break;
                }
            }
        }

        // Rotation Handling
        if (autoAim.getValue()) {
            LivingEntity primaryTarget = targets.get(0);
            handleRotation(player, primaryTarget);
        }

        // Bypass Heuristics
        if (currentConfig.bypassHeuristics) applyBypassHeuristics(player, targets);
    }

    private List<LivingEntity> getTargets(ClientPlayerEntity player) {
        return mc.world.getEntities().stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e != player)
                .filter(e -> !e.isDead())
                .filter(e -> e.isAlive())
                .filter(e -> !e.hasPassenger(player))
                .filter(e -> isTargetable(e, player))
                .filter(e -> e.squaredDistanceTo(player) <= (range.getValue().floatValue() + 0.5) * (range.getValue().floatValue() + 0.5))
                .collect(Collectors.toList());
    }

    private boolean isTargetable(LivingEntity entity, ClientPlayerEntity player) {
        if (entity instanceof PlayerEntity) return !player.isSpectator();
        if (entity instanceof HostileEntity) return true;
        if (entity instanceof PassiveEntity) return !entity.isTamed() || entity.getOwner() == player;
        return true;
    }

    private float getTargetScore(LivingEntity entity, ClientPlayerEntity player) {
        float score = 0;
        float distance = player.distanceTo(entity);
        score -= distance * 0.5f;
        score += entity.getHealth() * 0.1f;
        if (entity instanceof HostileEntity) score += 5;
        if (entity instanceof PlayerEntity) score += 3;
        if (canSee(player, entity)) score += 10;
        return score;
    }

    private boolean canSee(ClientPlayerEntity player, LivingEntity entity) {
        Vec3d eyePos = player.getEyePos();
        Vec3d targetPos = getAimPoint(entity);
        Vec3d direction = targetPos.subtract(eyePos).normalize();
        HitResult hit = mc.world.raycast(new Vec3d(eyePos), new Vec3d(eyePos).add(direction.multiply(100)));
        return hit.getType() == HitResult.Type.MISS || 
               (hit.getType() == HitResult.Type.ENTITY && ((net.minecraft.util.hit.EntityHitResult) hit).getEntity() == entity);
    }

    private Vec3d getAimPoint(LivingEntity entity) {
        return entity.getPos().add(0, entity.getStandingEyeHeight(), 0);
    }

    private void handleItemSwitch(ClientPlayerEntity player) {
        int bestSlot = -1;
        float bestScore = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem) {
                float damage = stack.getItem().getDamage();
                if (damage > bestScore) {
                    bestScore = damage;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != -1 && player.getInventory().selectedSlot != bestSlot &&
            System.currentTimeMillis() - lastSwitchTick > switchDelay.getValue().longValue()) {
            player.getInventory().selectedSlot = bestSlot;
            lastSwitchTick = (int) System.currentTimeMillis();
        }
    }

    private boolean canAttack(LivingEntity target, ClientPlayerEntity player) {
        float distance = player.distanceTo(target);
        return distance <= range.getValue().floatValue() &&
               player.getAttackCooldownProgress(0.5f) >= 0.9f &&
               !isAttacking;
    }

    private void attackTarget(ClientPlayerEntity player, LivingEntity target) {
        isAttacking = true;
        if (crit.getValue()) {
            if (player.fallDistance > 0.0f && !player.isOnGround() &&
                !player.isTouchingWater() && !player.isInLava() &&
                !player.getAbilities().flying) {
                player.fallDistance = 0.0f;
            }
        }
        player.attack(target);
        player.swingHand(Hand.MAIN_HAND);
        mc.execute(() -> isAttacking = false);
    }

    private void handleRotation(ClientPlayerEntity player, LivingEntity target) {
        Vec3d targetPos = getPredictedPosition(target);
        float[] targetRot = getRotations(player.getEyePos(), targetPos);

        // Silent Rotation Logic
        if (silent.getValue()) {
            // Calculate new silent yaw/pitch
            float newSilentYaw = slerp(player.getYaw(), targetRot[0], adaptiveLerpCurrent);
            float newSilentPitch = slerp(player.getPitch(), targetRot[1], adaptiveLerpCurrent);

            // Snap if close
            if (Math.abs(MathHelper.wrapDegrees(targetRot[0] - newSilentYaw)) < currentConfig.snapAngle &&
                Math.abs(targetRot[1] - newSilentPitch) < currentConfig.snapAngle) {
                newSilentYaw = targetRot[0];
                newSilentPitch = targetRot[1];
            }

            // Add Jitter
            if (currentConfig.jitterAmount > 0) {
                newSilentYaw += (random.nextFloat() - 0.5f) * currentConfig.jitterAmount;
                newSilentPitch += (random.nextFloat() - 0.5f) * currentConfig.jitterAmount;
            }

            // Store Silent Rotation
            silentYaw = newSilentYaw;
            silentPitch = MathHelper.clamp(newSilentPitch, -90, 90);

            // Send Packet
            sendRotationPacket(player, silentYaw, silentPitch);
        } else {
            // Normal Rotation (Moves Player)
            player.setYaw(targetRot[0]);
            player.setPitch(MathHelper.clamp(targetRot[1], -90, 90));
        }
    }

    private void resetSilentRotation(ClientPlayerEntity player) {
        if (silent.getValue()) {
            silentYaw = player.getYaw();
            silentPitch = player.getPitch();
            sendRotationPacket(player, silentYaw, silentPitch);
        }
    }

    private float slerp(float start, float target, float factor) {
        float diff = MathHelper.wrapDegrees(target - start);
        return start + diff * factor;
    }

    private void sendRotationPacket(ClientPlayerEntity player, float yaw, float pitch) {
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
            yaw, pitch, player.isOnGround()
        ));
    }

    private Vec3d getPredictedPosition(LivingEntity target) {
        if (!predictiveAim.getValue()) {
            return getAimPoint(target);
        }
        int ticks = currentConfig.predictionTicks;
        Vec3d pos = target.getPos();
        Vec3d vel = target.getVelocity();
        for (int i = 0; i < ticks; i++) {
            pos = pos.add(vel.multiply(currentConfig.velocityFactor));
        }
        return pos.add(0, target.getStandingEyeHeight(), 0);
    }

    private float[] getRotations(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{yaw, pitch};
    }

    private void applyBypassHeuristics(ClientPlayerEntity player, List<LivingEntity> targets) {
        long currentTick = mc.world.getTime();
        if (currentTick - lastBypassTick > 20) {
            lastBypassTick = currentTick;
            bypassActive = !bypassActive;
        }
        if (bypassActive && currentConfig.randomizeAttack) {
            // Subtle timing variation
            if