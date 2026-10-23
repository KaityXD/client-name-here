package hack.raze.client.handlers.impl;

import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventEarlyBeginFrame;
import hack.raze.client.handlers.Handler;
import hack.raze.client.handlers.RotationHandler;

/**
 * Advances the third-person silent-rotation preview once per frame so the
 * local model rotation tracks the sent rotation at render speed.
 */
public class RotationRenderHandler extends Handler {

    @EventSubscribe
    private void onFrame(EventEarlyBeginFrame event) {
        if (mc.player == null) return;
        RotationHandler.advanceRenderRotations();
    }
}
