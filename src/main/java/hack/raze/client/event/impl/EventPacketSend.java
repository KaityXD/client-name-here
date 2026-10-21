package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.protocol.Packet;

@Getter
@AllArgsConstructor
public class EventPacketSend extends Event {
    private Packet<?> packet;
}
