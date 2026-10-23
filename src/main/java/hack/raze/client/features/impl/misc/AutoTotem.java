package hack.raze.client.features.impl.misc;

import hack.raze.client.api.InventoryClickCompat;
import hack.raze.client.api.InventoryClickType;
import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventSystemUpdate;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

/** Keeps a totem in the offhand when one is available in the player's inventory. */
public final class AutoTotem extends Feature {
    private static final int OFFHAND_SWAP_BUTTON = 40;
    private static final int RETRY_DELAY_TICKS = 3;

    private int lastSwapTick = Integer.MIN_VALUE;

    public AutoTotem() {
        super(new FeatureInfo("AutoTotem", "Automatically equips a totem in the offhand", Category.UTILITY));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastSwapTick = Integer.MIN_VALUE;
    }

    @EventSubscribe
    private void onUpdate(EventSystemUpdate event) {
        if (!event.shouldRunGameTick() || isNull() || mc.gameMode == null) return;
        if (lastSwapTick != Integer.MIN_VALUE
                && mc.player.tickCount - lastSwapTick < RETRY_DELAY_TICKS) return;
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

        // Don't send inventory clicks against a chest, crafting menu, or other open container.
        if (mc.player.containerMenu != mc.player.inventoryMenu) return;

        int inventorySlot = findTotemSlot(mc.player.getInventory());
        if (inventorySlot < 0) return;

        int menuSlot = inventorySlot < 9 ? inventorySlot + 36 : inventorySlot;
        InventoryClickCompat.handleClick(
                mc.player.inventoryMenu.containerId,
                menuSlot,
                OFFHAND_SWAP_BUTTON,
                InventoryClickType.SWAP,
                mc.player
        );
        lastSwapTick = mc.player.tickCount;
    }

    private int findTotemSlot(Inventory inventory) {
        // Prefer the main inventory so the active hotbar slot is left alone.
        for (int slot = 9; slot < 36; slot++) {
            if (inventory.getItem(slot).is(Items.TOTEM_OF_UNDYING)) return slot;
        }
        for (int slot = 0; slot < 9; slot++) {
            if (inventory.getItem(slot).is(Items.TOTEM_OF_UNDYING)) return slot;
        }
        return -1;
    }
}
