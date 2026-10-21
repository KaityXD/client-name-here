package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.level.chunk.LevelChunk;

@AllArgsConstructor
@Getter
public class EventChunkLoad extends Event {
    private final LevelChunk chunk;
}
