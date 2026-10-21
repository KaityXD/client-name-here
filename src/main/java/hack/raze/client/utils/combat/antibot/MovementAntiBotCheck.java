package hack.raze.client.utils.combat.antibot;

import hack.raze.client.utils.Imports;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MovementAntiBotCheck implements AntiBotCheck, Imports {

    private final Set<UUID> movedEntities = new HashSet<>();

    @Override
    public boolean isBot(Player player) {
        return !hasEntityMoved(player);
    }

    private boolean hasEntityMoved(Player player) {
        if (mc.getConnection() == null) {
            return false;
        }

        if (player.xo != player.getX() || player.zo != player.getZ()) {
            movedEntities.add(player.getUUID());
        }

        return movedEntities.contains(player.getUUID());
    }
}
