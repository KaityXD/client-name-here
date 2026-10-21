package hack.raze.client.handlers.impl;

import hack.raze.client.Raze;
import hack.raze.client.command.Command;
import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventPacketSend;
import hack.raze.client.handlers.Handler;
import hack.raze.client.utils.ChatUtils;
import net.minecraft.network.protocol.game.ServerboundChatPacket;

import java.util.Arrays;
import java.util.List;

public class CommandHandler extends Handler {

    @EventSubscribe
    private void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof ServerboundChatPacket packet)) return;

        String message = packet.message();
        if (!message.startsWith(".")) {
            return;
        }

        event.cancel();
        if (mc.gui == null) return;

        String[] parts = message.trim().split("\\s+");
        Command command = Raze.commandManager.getCommand(parts[0].replace(".", ""));
        if (command == null) {
            ChatUtils.chat("Unknown command. Type .help for a list of commands.");
            return;
        }

        command.execute(Arrays.copyOfRange(parts, 1, parts.length));
    }
}
