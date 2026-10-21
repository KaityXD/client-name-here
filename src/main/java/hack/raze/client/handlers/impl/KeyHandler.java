package hack.raze.client.handlers.impl;

import org.lwjgl.glfw.GLFW;

import hack.raze.client.Raze;
import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventKey;
import hack.raze.client.event.impl.EventTick;
import hack.raze.client.features.Feature;
import hack.raze.client.handlers.Handler;
import hack.raze.client.handlers.InputHandler;
import net.minecraft.client.Minecraft;

import java.util.HashMap;


public class KeyHandler extends Handler {
    @EventSubscribe
    public void onKey(EventKey event) {
        // Needed for JNT
        if (hack.raze.client.api.MinecraftCompat.getScreen() != null)
            return;

        if (event.getAction() != GLFW.GLFW_PRESS)
            return;

        for (Feature feature : Raze.featureManager.getFeatures()) {
            if (feature.getKey() != -1 && feature.getKey() == event.getKey()) {
                feature.toggle();
            }
        }
    }

    private final HashMap<Integer, Boolean> prevMouseState = new HashMap<>();

    @EventSubscribe
    public void onTick(EventTick tick) {
        if (hack.raze.client.api.MinecraftCompat.getScreen() != null) return;

        HashMap<Integer, Boolean> pressedThisTick = new HashMap<>();
        HashMap<Integer, Boolean> downThisTick = new HashMap<>();

        for (Feature feature : Raze.featureManager.getFeatures()) {
            int key = feature.getKey();
            if (key == -1) continue;
            if ((key & 0x80000000) == 0) continue;

            int mb = key & 0xFF;
            boolean pressed = pressedThisTick.computeIfAbsent(mb, b -> {
                boolean down = InputHandler.isMouseDown(b);
                downThisTick.put(b, down);
                return down && !prevMouseState.getOrDefault(b, false);
            });

            if (pressed) {
                feature.toggle();
            }
        }

        for (var entry : downThisTick.entrySet()) {
            prevMouseState.put(entry.getKey(), entry.getValue());
        }
    }
}
