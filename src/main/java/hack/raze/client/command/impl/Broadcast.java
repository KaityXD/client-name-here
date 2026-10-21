//? if debug {
/*package hack.raze.client.command.impl;

import hack.raze.client.command.Command;
import hack.raze.client.handlers.impl.BroadcastHandler;
import hack.raze.client.utils.ChatUtils;

public class Broadcast extends Command {

    public Broadcast() {
        super("broadcast", "Dev test command");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            ChatUtils.chat("usage: .broadcast <message>");
            return;
        }

        BroadcastHandler.addMessage(String.join(" ", args));
    }
}
*///?}