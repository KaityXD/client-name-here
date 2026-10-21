package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import lombok.Getter;

public class SprintEvent extends Event {

    private boolean shouldSprint;
    @Getter
    private boolean overridden;

    public SprintEvent(boolean shouldSprint) {
        this.shouldSprint = shouldSprint;
        this.overridden = false;
    }

    public boolean shouldSprint() {
        return shouldSprint;
    }

    public void setShouldSprint(boolean shouldSprint) {
        this.shouldSprint = shouldSprint;
        this.overridden = true;
    }

}
