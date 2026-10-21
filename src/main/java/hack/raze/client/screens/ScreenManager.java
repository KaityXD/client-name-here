package hack.raze.client.screens;

import hack.raze.client.features.settings.Setting;
import hack.raze.client.screens.clickgui.glassrewrite.GlassScreen;
import hack.raze.client.utils.Imports;

public class ScreenManager implements Imports {
    public static Setting focusedSetting = null;

    private GlassScreen glassClickGuiScreen;
    
    public void initialize() {
        glassClickGuiScreen = new GlassScreen();
    }
    
    public void displayClickGUI() {
        hack.raze.client.api.MinecraftCompat.setScreen(glassClickGuiScreen);
    }
    
    public boolean isClickGUIOpen() {
        return hack.raze.client.api.MinecraftCompat.getScreen() == glassClickGuiScreen;
    }
    
    public void closeAllScreens() {
        focusedSetting = null;
        hack.raze.client.api.MinecraftCompat.setScreen(null);
    }

    public void destroy() {
        closeAllScreens();
        glassClickGuiScreen = null;
    }
}
