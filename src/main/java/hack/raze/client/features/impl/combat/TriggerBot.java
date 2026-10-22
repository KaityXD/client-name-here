package hack.raze.client.features.impl.combat;

import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventHandleInput;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.BoolSetting;
import hack.raze.client.features.settings.impl.IntSetting;
import hack.raze.client.features.settings.impl.ItemPickerSetting;
import hack.raze.client.features.settings.impl.ModeSetting;
import hack.raze.client.features.settings.impl.RangeSetting;
import hack.raze.client.handlers.InputHandler;
import hack.raze.client.handlers.impl.HurtTickHandler;
import hack.raze.client.utils.combat.CombatUtils;
import hack.raze.client.utils.combat.TargetUtils;
import hack.raze.client.utils.inventory.InventoryUtils;
import hack.raze.client.utils.math.TimerUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Attacks valid entities under the crosshair using Raze's input events. */
public final class TriggerBot extends Feature {
    private final ModeSetting critMode = new ModeSetting("Crit Mode", "pCrit", "pCrit", "normal", "None");
    private final BoolSetting requireSpaceToCrit = new BoolSetting("Crit only on space", false);
    private final BoolSetting attackShieldedTargets = new BoolSetting("Attack Shields", true);
    private final BoolSetting hurtTimeCheck = new BoolSetting("HurtTime Check", true);
    private final ItemPickerSetting itemWhitelist = new ItemPickerSetting("Item Whitelist");
    private final RangeSetting attackCooldown = new RangeSetting("Cooldown", 90, 100, 0, 100, 1);
    private final RangeSetting critCooldown = new RangeSetting("Crit Cooldown", 70, 89, 0, 100, 1);
    private final RangeSetting critRange = new RangeSetting("Crit Range", 1.0F, 2.2F, 0, 6, 0.1F);
    private final IntSetting reactionDelay = new IntSetting("Reaction Delay", 0, 0, 500);
    private final IntSetting attackDelay = new IntSetting("Attack Delay", 0, 0, 500);
    private final BoolSetting requireClick = new BoolSetting("Require Attack Key", false);
    private final BoolSetting clickSimulation = new BoolSetting("Click Simulation", false);

    private final TimerUtils reactionTimer = new TimerUtils();
    private final TimerUtils attackTimer = new TimerUtils();
    private LivingEntity trackedTarget;
    private int lastAttackTick = -1;

    public TriggerBot() {
        super(new FeatureInfo("TriggerBot", "Attacks a valid target when it is under your crosshair", Category.COMBAT));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reactionTimer.reset();
        attackTimer.reset();
        trackedTarget = null;
        lastAttackTick = -1;
    }

    @Override
    public CharSequence concat() {
        return critMode.is("None") ? "" : critMode.getValue();
    }

    @EventSubscribe
    private void onInput(EventHandleInput.Early event) {
        if (isNull() || mc.gameMode == null) {
            resetTarget();
            return;
        }

        LivingEntity target = crosshairTarget();
        if (target == null || !TargetUtils.isTargetAllowed(target) || TargetUtils.resolveTarget(target) != target) {
            resetTarget();
            return;
        }

        if (trackedTarget != target) {
            trackedTarget = target;
            reactionTimer.reset();
        }
        if (target.isDeadOrDying() || !target.isAlive()) return;
        if (hurtTimeCheck.getValue() && HurtTickHandler.isCurrentlyHurt(target)) return;
        if (!attackShieldedTargets.getValue() && target.isBlocking()) return;
        if (requireClick.getValue() && !InputHandler.isBindDown(mc.options.keyAttack)) return;
        if (!isAllowedItem()) return;
        if (!reactionTimer.hasReached(reactionDelay.getValue())) return;
        if (!attackTimer.hasReached(attackDelay.getValue())) return;
        if (lastAttackTick == mc.player.tickCount || !canAttackNow(target)) return;

        mc.hitResult = new EntityHitResult(target);
        mc.crosshairPickEntity = target;
        InputHandler.simulateClick(mc.options.keyAttack, clickSimulation.getValue());
        attackTimer.reset();
        lastAttackTick = mc.player.tickCount;
    }

    private LivingEntity crosshairTarget() {
        if (!(mc.hitResult instanceof EntityHitResult hit) || hit.getType() != HitResult.Type.ENTITY) return null;
        return hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    private boolean canAttackNow(LivingEntity target) {
        float required = attackCooldown.getRandom() / 100.0F;
        boolean withinCritRange = mc.player.distanceTo(target) >= critRange.getMinValue()
                && mc.player.distanceTo(target) <= critRange.getMaxValue();
        if (withinCritRange && !critMode.is("None") && CombatUtils.canCrit()) {
            required = critCooldown.getRandom() / 100.0F;
        }
        if (mc.player.getAttackStrengthScale(required) < required) return false;
        if (critMode.is("None") || !withinCritRange || CombatUtils.canCrit()) return true;
        if (requireSpaceToCrit.getValue() && !mc.options.keyJump.isDown()) return false;
        return mc.player.onGround() || mc.player.isFallFlying() || mc.player.isInWater()
                || mc.player.onClimbable();
    }

    private boolean isAllowedItem() {
        if (itemWhitelist.getSelectedCount() == 0) return true;
        ItemStack held = InventoryUtils.getMainHandItem();
        if (held.isEmpty()) return itemWhitelist.isSelected(Items.AIR);
        return itemWhitelist.isSelected(held.getItem());
    }

    private void resetTarget() {
        if (trackedTarget == null) return;
        trackedTarget = null;
        reactionTimer.reset();
    }
}
