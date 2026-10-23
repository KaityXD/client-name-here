package hack.raze.client.features.impl.utility;

import hack.raze.client.api.InventoryClickCompat;
import hack.raze.client.api.InventoryClickType;
import hack.raze.client.api.MinecraftCompat;
import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventClientPlayerTick;
import hack.raze.client.event.impl.EventPacketReceive;
import hack.raze.client.event.impl.EventPacketSend;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.BoolSetting;
import hack.raze.client.features.settings.impl.IntSetting;
import hack.raze.client.mixin.accessors.AbstractContainerScreenAccessor;
import hack.raze.client.utils.inventory.InventoryUtils;
import hack.raze.client.utils.math.TimerUtils;
import hack.raze.client.utils.strings.Concat;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

public class HoverTotem extends Feature {

    private final BoolSetting offhand = new BoolSetting(Concat.of("Offhand"), true);
    private final BoolSetting hotbar = new BoolSetting(Concat.of("Hotbar"), true);
    private final IntSetting hotbarSlot = new IntSetting(Concat.of("Hotbar Slot"), 1, 1, 9, o -> hotbar.getValue());
    private final IntSetting delay = new IntSetting(Concat.of("Delay"), 150, 0, 1000, " ms");
    private final BoolSetting autoOpen = new BoolSetting(Concat.of("Auto Open"), true);
    private final BoolSetting keepOpen = new BoolSetting(Concat.of("Keep Open"), true);

    private final TimerUtils timer = new TimerUtils();
    private boolean pendingSetup;
    private boolean forcedOpen;

    public HoverTotem() {
        super(new FeatureInfo(
                Concat.of("Hover Totem"),
                Concat.of("Moves hovered totems into your offhand and hotbar"),
                Category.UTILITY
        ));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }

    private void reset() {
        pendingSetup = false;
        forcedOpen = false;
        timer.reset();
    }

    @EventSubscribe
    private void onTick(EventClientPlayerTick event) {
        if (isNull()) return;

        if (pendingSetup) {
            if (hasTotemSetup() || !hasMovableTotem()) {
                pendingSetup = false;
                closeForced();
            } else if (!keepOpen.getValue() && !(MinecraftCompat.getScreen() instanceof InventoryScreen)) {
                forcedOpen = false;
            }
        }

        if (MinecraftCompat.getScreen() instanceof InventoryScreen screen) {
            handleHover(screen);
        }
    }

    private void handleHover(InventoryScreen screen) {
        if (!offhand.getValue() && !hotbar.getValue()) return;

        Slot slot = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
        if (slot == null || !slot.hasItem() || !slot.getItem().is(Items.TOTEM_OF_UNDYING)) return;
        if (slot.index == InventoryMenu.SHIELD_SLOT) return;

        boolean needOffhand = needsOffhand();
        boolean needHotbar = needsHotbar();
        if (!needOffhand && !needHotbar) return;
        if (!timer.hasElapsedTime(delay.getValue())) return;

        int button = needOffhand ? Inventory.SLOT_OFFHAND : hotbarSlot.getValue() - 1;
        InventoryClickCompat.handleClick(
                mc.player.containerMenu.containerId,
                slot.index,
                button,
                InventoryClickType.SWAP,
                mc.player
        );
        timer.reset();
    }

    private boolean needsOffhand() {
        return offhand.getValue() && !mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
    }

    private boolean needsHotbar() {
        return hotbar.getValue() && !hasTotemInSelectedSlot();
    }

    private boolean hasTotemSetup() {
        if (isNull()) return true;
        boolean offhandReady = !offhand.getValue() || mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
        boolean hotbarReady = !hotbar.getValue() || hasTotemInSelectedSlot();
        return offhandReady && hotbarReady;
    }

    private boolean hasTotemInSelectedSlot() {
        return mc.player.getInventory().getItem(hotbarSlot.getValue() - 1).is(Items.TOTEM_OF_UNDYING);
    }

    private boolean hasMovableTotem() {
        return !isNull() && (InventoryUtils.findTotemSlot() != -1
                || InventoryUtils.hasItemInHotbar(item -> item == Items.TOTEM_OF_UNDYING));
    }

    private void openInventoryIfClosed() {
        if (isNull()) return;
        if (MinecraftCompat.getScreen() == null) {
            forcedOpen = true;
            MinecraftCompat.setScreen(new InventoryScreen(mc.player));
        }
    }

    private void closeForced() {
        if (!forcedOpen) return;
        forcedOpen = false;
        if (isNull()) return;
        if (MinecraftCompat.getScreen() instanceof InventoryScreen) {
            mc.player.closeContainer();
        }
    }

    @EventSubscribe
    private void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof ClientboundEntityEventPacket packet)) return;
        if (packet.getEventId() != EntityEvent.PROTECTED_FROM_DEATH) return;
        mc.execute(() -> handleTotemPop(packet));
    }

    private void handleTotemPop(ClientboundEntityEventPacket packet) {
        if (!isEnabled() || isNull()) return;
        Entity entity = packet.getEntity(mc.level);
        if (entity != mc.player) return;
        if (!autoOpen.getValue() && !keepOpen.getValue()) return;
        if (!hasMovableTotem()) return;

        pendingSetup = true;
        if (autoOpen.getValue()) {
            openInventoryIfClosed();
        }
    }

    @EventSubscribe
    private void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof ServerboundContainerClosePacket)) return;
        if (!pendingSetup || !keepOpen.getValue()) return;
        if (!(MinecraftCompat.getScreen() instanceof InventoryScreen)) return;

        if (hasTotemSetup() || !hasMovableTotem()) {
            pendingSetup = false;
            forcedOpen = false;
            return;
        }

        event.cancel();
        mc.execute(this::reopenForced);
    }

    private void reopenForced() {
        if (!isEnabled() || !pendingSetup || isNull()) return;
        if (MinecraftCompat.getScreen() == null) {
            forcedOpen = true;
            MinecraftCompat.setScreen(new InventoryScreen(mc.player));
        }
    }
}
