package hack.raze.client.features.impl.combat;

import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventOnAttackEntity;
import hack.raze.client.event.impl.EventPacketReceive;
import hack.raze.client.event.impl.EventTick;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.IntSetting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.world.entity.Entity;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Delays movement updates for the last attacked entity. */
public final class Backtrack extends Feature {
    private final IntSetting delay = new IntSetting("Delay", 180, 0, 1000, " ms");
    private final IntSetting range = new IntSetting("Range", 6, 1, 16, " blocks");

    private final Queue<DelayedPacket> delayedPackets = new ConcurrentLinkedQueue<>();
    private volatile Entity target;

    public Backtrack() {
        super(new FeatureInfo("Backtrack", "Delays target movement updates to preserve their previous position", Category.COMBAT));
    }

    @Override
    public void onEnable() {
        delayedPackets.clear();
        target = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        flushAll();
        target = null;
        super.onDisable();
    }

    @EventSubscribe
    private void onAttack(EventOnAttackEntity.Pre event) {
        if (isNull() || event.getTarget() == null || event.getTarget() == target) return;
        flushAll();
        target = event.getTarget();
    }

    @EventSubscribe
    private void onPacketReceive(EventPacketReceive event) {
        if (isNull() || mc.isSingleplayer()) {
            flushAll();
            target = null;
            return;
        }

        Entity currentTarget = target;
        if (!isValidTarget(currentTarget)) {
            flushAll();
            target = null;
            return;
        }

        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ClientboundMoveEntityPacket movePacket)) return;
        Entity moved = movePacket.getEntity(mc.level);
        if (moved == null || moved.getId() != currentTarget.getId()) return;

        int delayMs = delay.getValue();
        if (delayMs <= 0) return;

        delayedPackets.add(new DelayedPacket(packet, System.currentTimeMillis()));
        event.cancel();
        flushExpired(delayMs);
    }

    @EventSubscribe
    private void onTick(EventTick event) {
        if (!isValidTarget(target)) {
            flushAll();
            target = null;
            return;
        }
        flushExpired(delay.getValue());
    }

    private boolean isValidTarget(Entity entity) {
        return entity != null && entity.isAlive() && !isNull()
                && mc.player.distanceToSqr(entity) <= (double) range.getValue() * range.getValue();
    }

    private void flushExpired(long delayMs) {
        long now = System.currentTimeMillis();
        while (true) {
            DelayedPacket queued = delayedPackets.peek();
            if (queued == null || now - queued.timestamp < delayMs) return;
            delayedPackets.poll();
            handle(queued.packet);
        }
    }

    private void flushAll() {
        DelayedPacket queued;
        while ((queued = delayedPackets.poll()) != null) {
            handle(queued.packet);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void handle(Packet<?> packet) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return;
        try {
            ((Packet) packet).handle((PacketListener) connection);
        } catch (Throwable ignored) {
            // The connection may have closed while a delayed packet was queued.
        }
    }

    private record DelayedPacket(Packet<?> packet, long timestamp) {}
}
