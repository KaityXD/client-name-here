package hack.raze.client.api;

import net.minecraft.client.Minecraft;

public final class ScreenCompat {

    private ScreenCompat() {
    }

    public static boolean hasScreen(Minecraft minecraft) {
        if (minecraft == null) {
            return false;
        }

        //? if >26.1.2 {
        /*return minecraft.gui.screen() != null;
        *///?} else {
        return minecraft.screen != null;
        //?}
    }
}
