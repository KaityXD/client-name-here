package hack.raze.client.utils.combat.antibot;

import net.minecraft.world.entity.player.Player;

public final class NameAntiBotCheck implements AntiBotCheck {

    @Override
    public boolean isBot(Player player) {
        String name = getBotCheckName(player);
        if (name.isEmpty()) {
            return true;
        }

        if (name.contains(":")) {
            return true;
        }

        if (name.contains("+")) {
            return true;
        }

        if (name.contains(" ")) {
            return true;
        }

        if (name.startsWith("cit")) {
            return true;
        }

        return name.startsWith("npc");
    }

    private String getBotCheckName(Player player) {
        if (player.getDisplayName() == null) {
            return "";
        }

        String name = player.getDisplayName().getString();
        if (name == null) {
            return "";
        }

        return name.toLowerCase();
    }
}
