package hack.raze.client.utils.combat.antibot;

import hack.raze.client.utils.Imports;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;

public final class TabListAntiBotCheck implements AntiBotCheck, Imports {

    @Override
    public boolean isBot(Player player) {
        if (mc.getConnection() == null) {
            return false;
        }

        PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
        return info == null;
    }
}
