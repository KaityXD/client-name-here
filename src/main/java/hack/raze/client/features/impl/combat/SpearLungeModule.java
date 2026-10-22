package hack.raze.client.features.impl.combat;

import hack.raze.client.api.MinecraftCompat;
import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventHandleInput;
import hack.raze.client.event.impl.EventStartAttack;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.BoolSetting;
import hack.raze.client.features.settings.impl.IntSetting;
import hack.raze.client.features.settings.impl.KeybindSetting;
import hack.raze.client.features.settings.impl.ModeSetting;
import hack.raze.client.features.settings.impl.ItemPickerSetting;
import hack.raze.client.handlers.InputHandler;
import hack.raze.client.utils.inventory.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

/** Switches to a Lunge spear and sends the spear-stab action for Raze. */
public final class SpearLungeModule extends Feature {
    private final ModeSetting mode = new ModeSetting("Mode", "Attack", "Attack", "Keypress");
    private final BoolSetting silent = new BoolSetting("Silent", false);
    private final IntSetting cooldownPercent = new IntSetting("Cooldown %", 85, 0, 100);
    private final IntSetting multiplier = new IntSetting("Multiplier", 1, 1, 10);
    private final ItemPickerSetting itemWhitelist = new ItemPickerSetting("Item Whitelist", setting -> mode.is("Attack"));
    private final KeybindSetting activateKey = new KeybindSetting("Activate Key", -1, setting -> mode.is("Keypress"));

    private int originalSlot = -1;
    private int lastActionTick = -1;
    private boolean swapPending;
    private boolean keyWasDown;

    public SpearLungeModule() {
        super(new FeatureInfo("Spear Lunge", "Uses a Lunge spear to propel you forward", Category.COMBAT));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastActionTick = -1;
        keyWasDown = false;
        swapPending = false;
    }

    @Override
    public void onDisable() {
        if (!isNull() && swapPending) restoreSlot();
        originalSlot = -1;
        swapPending = false;
        keyWasDown = false;
        super.onDisable();
    }

    @EventSubscribe
    private void onAttackPre(EventStartAttack.Pre event) {
        if (!mode.is("Attack") || !canActivate() || isHoldingLungeSpear()) return;
        if (!isAllowedHeldItem()) return;
        beginSpearSwap();
    }

    @EventSubscribe
    private void onAttack(EventStartAttack event) {
        if (!canActivate()) return;
        if (!isHoldingLungeSpear() && !(silent.getValue() && swapPending)) return;
        event.cancel();
        sendStab();
        lastActionTick = mc.player.tickCount;
        if (silent.getValue() && swapPending) restoreSlot();
    }

    @EventSubscribe
    private void onKeypress(EventHandleInput.Early event) {
        if (!mode.is("Keypress") || !canActivate() || swapPending) return;
        boolean down = InputHandler.isBindDown(activateKey.getKey());
        if (down && !keyWasDown && !isHoldingLungeSpear()) {
            if (beginSpearSwap()) {
                sendStab();
                lastActionTick = mc.player.tickCount;
            }
        }
        keyWasDown = down;
    }

    @EventSubscribe
    private void onInputPost(EventHandleInput.Post event) {
        if (!swapPending || silent.getValue() || isNull()) return;
        if (mc.player.tickCount > lastActionTick) restoreSlot();
    }

    private boolean canActivate() {
        return isEnabled() && !isNull() && mc.gameMode != null
                && MinecraftCompat.getScreen() == null && !mc.isPaused()
                && lastActionTick != mc.player.tickCount;
    }

    private int findLungeSpear() {
        return InventoryUtils.findItemWithEnchantmentInHotbar(
                stack -> InventoryUtils.isItemSpear(stack.getItem()), Enchantments.LUNGE, false);
    }

    private boolean isHoldingLungeSpear() {
        int slot = mc.player.getInventory().getSelectedSlot();
        return slot == findLungeSpear();
    }

    private boolean isAllowedHeldItem() {
        if (itemWhitelist.getSelectedCount() == 0) return true;
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) return itemWhitelist.isSelected(Items.AIR);
        return itemWhitelist.isSelected(held.getItem());
    }

    private boolean beginSpearSwap() {
        int spearSlot = findLungeSpear();
        if (spearSlot < 0 || spearSlot == mc.player.getInventory().getSelectedSlot()) return false;
        if (mc.player.getAttackStrengthScale(0.0F) < cooldownPercent.getValue() / 100.0F) return false;

        originalSlot = mc.player.getInventory().getSelectedSlot();
        if (silent.getValue()) InventoryUtils.silentSwapTo(spearSlot);
        else InventoryUtils.setInvSlot(spearSlot);
        swapPending = true;
        return true;
    }

    private void sendStab() {
        for (int i = 0; i < multiplier.getValue(); i++) {
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STAB, BlockPos.ZERO, Direction.DOWN));
        }
    }

    private void restoreSlot() {
        if (originalSlot >= 0 && originalSlot <= 8) {
            if (silent.getValue()) InventoryUtils.silentSwapBack();
            else InventoryUtils.setInvSlot(originalSlot);
        }
        originalSlot = -1;
        swapPending = false;
    }
}
