package hack.raze.client.event.impl;

import hack.raze.client.event.Event;

public class MousePressEvent extends Event {

    public final int button;
    public final int action;

    public MousePressEvent(int button, int action) {
        this.button = button;
        this.action = action;
    }
}