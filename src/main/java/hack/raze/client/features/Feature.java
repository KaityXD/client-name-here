package hack.raze.client.features;

//? if auth
//import hack.raze.client.Raze;
import hack.raze.client.Raze;
import hack.raze.client.utils.Imports;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;

import hack.raze.client.event.EventManager;
import hack.raze.client.features.settings.Setting;
import net.minecraft.client.Minecraft;

public class Feature implements Imports {
    protected static final Minecraft mc = Minecraft.getInstance();
    public ArrayList<Setting> settings = new ArrayList<>();
    private boolean enabled = false;
    @Getter
    private int key;
    private boolean visible;

    private final FeatureInfo info;

    public Feature(FeatureInfo info) {
        this.info = info;
        this.key = info != null ? info.key() : -1;
        this.visible = info == null || info.visible();
    }

    public void initSettings() {
        settings.clear();

        Class<?> currentClass = this.getClass();
        while (currentClass != null && Feature.class.isAssignableFrom(currentClass)) {
            addSettingsFromClass(currentClass);
            currentClass = currentClass.getSuperclass();
        }
    }

    private void addSettingsFromClass(Class<?> sourceClass) {
        Field[] fields = sourceClass.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if (!(value instanceof Setting setting)) continue;
                if (settings.contains(setting)) continue;
                settings.add(setting);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public CharSequence getName() {
        if (info == null) return this.getClass().getSimpleName();
        CharSequence name = info.name();
        if (name == null || name.length() == 0) {
            return this.getClass().getSimpleName();
        }
        return name;
    }

    public CharSequence getDescription() {
        if (info == null) return "";
        CharSequence description = info.description();
        return description != null ? description : "";
    }

    public Category getCategory() {
        return info != null ? info.category() : Category.INTERNALS;
    }

    public void onEnable() {
        // "+auth-related"
        //? if auth {
        /*if (!validateAuth()) {
            this.enabled = false;
            return;
        }
        *///?}
        EventManager.register(this);
    }

    public void onDisable() {
        EventManager.unregister(this);
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (this.enabled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
        saveAutosave();
    }

    protected boolean shouldNotifyToggle() {
        return true;
    }
    
    //? if auth {
    /*// Distributed auth validation - checks multiple conditions
    private boolean validateAuth() {
        if (Raze.authManager == null) return false;
        int delta = Raze.authManager.getAuthFingerprint() ^ Raze.authManager.getAuthFingerprintTarget();
        delta |= delta >> 4;
        delta |= delta >> 2;
        delta |= delta >> 1;

        boolean fingerprintOk = (delta & 0x01) == 0;
        return fingerprintOk
            && Raze.authManager.validateUid()
            && !Raze.authManager.isSessionExpired();
    }
    *///?}

    public Boolean hasSettings() {
        return !settings.isEmpty();
    }

    public void setKey(int key) {
        if (this.key == key) return;
        this.key = key;
        saveAutosave();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) return;
        this.visible = visible;
        saveAutosave();
    }

    public String getInfo() {
        return "";
    }

    public CharSequence concat() {
        return "";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isNull() {
        return mc.player == null || mc.level == null;
    }

    private void saveAutosave() {
        if (Raze.featureConfig == null || Raze.featureConfig.loading) return;
        Raze.featureConfig.saveProfile("_autosave");
    }
}
