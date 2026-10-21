package hack.raze.client.utils.combat.antibot;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class ScaleAntiBotCheck implements AntiBotCheck {

    private static final double MIN_VALID_PLAYER_SCALE = 0.75;
    private static final double MAX_VALID_PLAYER_SCALE = 1.25;

    @Override
    public boolean isBot(Player player) {
        double scale = player.getAttributeValue(Attributes.SCALE);
        return scale < MIN_VALID_PLAYER_SCALE || scale > MAX_VALID_PLAYER_SCALE;
    }
}
