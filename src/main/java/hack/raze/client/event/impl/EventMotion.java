package hack.raze.client.event.impl;

import hack.raze.client.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventMotion extends Event {
    private double x, y, z;
    private boolean onGround;
    private double yaw, pitch;

    public EventMotion(Event.Stage stage, double x, double y, double z, boolean onGround, double yaw, double pitch) {
        super(stage);
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static class Pre extends EventMotion {
        public Pre(double x, double y, double z, boolean onGround, double yaw, double pitch) {
            super(Event.Stage.PRE, x, y, z, onGround, yaw, pitch);
        }
    }

    public static class Post extends EventMotion {
        public Post(double x, double y, double z, boolean onGround, double yaw, double pitch) {
            super(Event.Stage.POST, x, y, z, onGround, yaw, pitch);
        }
    }
}
