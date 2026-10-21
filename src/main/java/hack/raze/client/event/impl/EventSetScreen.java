package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screens.Screen;

@Getter
@Setter
public class EventSetScreen extends Event {
    private Screen screen;

    public EventSetScreen(Screen screen) {
        this.screen = screen;
    }

}
