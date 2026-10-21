//? if auth {
/*package hack.raze.client.auth;

import hack.raze.client.Raze;
import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventTick;
import hack.raze.client.handlers.Handler;

// "+auth-related"

public class AuthHandler extends Handler {
    
    @EventSubscribe(priority = EventSubscribe.Priority.HIGHEST)
    public void onTick(EventTick event) {
        if (Raze.authManager != null && Raze.authManager.isConnectionTimeout()) {
            hangClient();
            event.cancel();
            return;
        }
        
        if (Raze.featureManager == null) {
            hangClient();
            event.cancel();
        }
    }
    
    private void hangClient() {
        Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
            }
        }
    }
}
*///?}
