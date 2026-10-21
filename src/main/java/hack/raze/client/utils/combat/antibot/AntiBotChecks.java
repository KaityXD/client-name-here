package hack.raze.client.utils.combat.antibot;

import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class AntiBotChecks {

    private static final List<AntiBotCheck> CHECKS = List.of(
            new NameAntiBotCheck(),
            new BlissAntiBotCheck(),
            new MovementAntiBotCheck(),
            new ScaleAntiBotCheck(),
            new TabListAntiBotCheck()
    );

    private AntiBotChecks() {
    }

    public static boolean isBot(Player player) {
        for (AntiBotCheck check : CHECKS) {
            if (!check.isBot(player)) {
                continue;
            }

            return true;
        }

        return false;
    }
}
