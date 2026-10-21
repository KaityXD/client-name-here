package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*
 * Fired when the player's selected hotbar slot is about to change.
 */
@AllArgsConstructor
@Getter
@Setter
public class EventHotbarChange extends Event {
    private int previousSlot;
    private int newSlot;
}
