package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import hack.raze.client.api.GuiGraphicsCompat;
import lombok.Getter;
import net.minecraft.client.DeltaTracker;

@Getter
public class EventRenderScoreboardSidebar extends Event {
    private final GuiGraphicsCompat context;
    private final DeltaTracker deltaTracker;

    public EventRenderScoreboardSidebar(Stage stage, GuiGraphicsCompat context, DeltaTracker deltaTracker) {
        super(stage);
        this.context = context;
        this.deltaTracker = deltaTracker;
    }

    public static class Pre extends EventRenderScoreboardSidebar {
        public Pre(GuiGraphicsCompat context, DeltaTracker deltaTracker) {
            super(Stage.PRE, context, deltaTracker);
        }
    }
}
