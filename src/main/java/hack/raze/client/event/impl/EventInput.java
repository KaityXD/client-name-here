package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import net.minecraft.client.player.ClientInput;

import static hack.raze.client.utils.Imports.mc;

public class EventInput extends Event {

    public ClientInput input;
    public boolean check;

    public EventInput(ClientInput input) {
        this.input = input;
        this.check = (mc.player != null && input == mc.player.input);
    }
}