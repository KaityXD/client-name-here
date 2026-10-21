package hack.raze.client.features.impl.combat;

import hack.raze.client.Raze;
import hack.raze.client.api.ScreenCompat;
import hack.raze.client.event.EventSubscribe;
import hack.raze.client.event.impl.EventStartAttack;
import hack.raze.client.event.impl.EventTick;
import hack.raze.client.features.Category;
import hack.raze.client.features.Feature;
import hack.raze.client.features.FeatureInfo;
import hack.raze.client.features.settings.impl.BoolSetting;
import hack.raze.client.features.settings.impl.IntSetting;
import hack.raze.client.features.settings.impl.MultiModeSetting;
import hack.raze.client.handlers.impl.SwapStateManager;
import hack.raze.client.utils.inventory.InventoryUtils;
import hack.raze.client.utils.player.PlayerUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;

public class ShieldStun extends Feature {

    private final MultiModeSetting w = new MultiModeSetting("Weapon Types", "Sword", "Other");
    private final BoolSetting st = new BoolSetting("Stun", true);
    private final IntSetting sd = new IntSetting("Stun Delay", 50, 0, 100, " ms", o -> st.getValue());
    private final BoolSetting rs = new BoolSetting("Restore", true);
    private final IntSetting rd = new IntSetting("Restore Delay", 0, 0, 20, " ticks", o -> rs.getValue());

    private LivingEntity tg;
    private long dl;

    public ShieldStun() {
        super(new FeatureInfo("Shield Stun", "Stun Opponent's Shield When Attack", Category.COMBAT));
        w.toggle("Sword");
    }

    @EventSubscribe
    private void ca(EventStartAttack e) {
        if (!isEnabled() || isNull()) return;
        if (dl != 0) return;
        if (ScreenCompat.hasScreen(mc)) return;
        if (mc.player.getMainHandItem().getItem() instanceof AxeItem) return;

        LivingEntity t = PlayerUtils.getManualSelectTarget();
        if (!(t instanceof Player p)) return;
        if (p == mc.player || !p.isAlive()) return;
        if (!blocking(p) || !fc(p)) return;
        if (!wc()) return;

        int c = SwapStateManager.getSelectedSlot();
        int a = fa();
        if (a == -1 || a == c) return;

        tg = p;
        long d = st.getValue() ? sd.getValue() : 0;
        if (d > 0) dl = System.currentTimeMillis() + d;

        SwapStateManager.swapTo(this, a, false, -1);
        e.cancel();

        an(p);

        if (dl == 0) nx();
    }

    @EventSubscribe
    private void tk(EventTick e) {
        if (!isEnabled() || isNull()) return;

        if (ScreenCompat.hasScreen(mc)) {
            if (dl != 0) {
                SwapStateManager.cancel(this);
                dl = 0;
                tg = null;
            }
            return;
        }

        if (dl == 0) return;
        if (System.currentTimeMillis() < dl) return;
        dl = 0;
        nx();
    }

    @Override
    public void onDisable() {
        SwapStateManager.cancel(this);
        dl = 0;
        tg = null;
    }

    private void nx() {
        dl = 0;
        if (isNull() || mc.gameMode == null || tg == null || !tg.isAlive()) {
            SwapStateManager.cancel(this);
            tg = null;
            return;
        }

        if (SwapStateManager.isActive(this)
                && mc.player.getInventory().getSelectedSlot() != SwapStateManager.getActiveTargetSlot(this)) {
            SwapStateManager.cancel(this, false);
            tg = null;
            return;
        }

        if (st.getValue()) {
            MaceSwap ms = Raze.featureManager == null ? null : Raze.featureManager.getFeatureByClass(MaceSwap.class);
            int m = ms != null ? ms.maceForSmash() : -1;
            if (m != -1) SwapStateManager.swapTo(this, m, false, -1);

            an(tg);
        }

        if (rs.getValue()) {
            int r = SwapStateManager.getActiveTargetSlot(this);
            if (r != -1) SwapStateManager.swapTo(this, r, false, rd.getValue() + 1);
        } else {
            SwapStateManager.cancel(this, false);
        }
        tg = null;
    }

    private void an(LivingEntity t) {
        if (mc.gameMode == null || mc.player == null || t == null) return;
        mc.gameMode.attack(mc.player, t);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private boolean wc() {
        Item h = mc.player.getMainHandItem().getItem();
        if (w.isEnabled("Other")) return true;
        if (w.isEnabled("Sword") && (InventoryUtils.isSword(h) || h == Items.COPPER_SWORD)) return true;
        return false;
    }

    private int fa() {
        int b = -1;
        int s = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack q = mc.player.getInventory().getItem(i);
            if (q.isEmpty() || !(q.getItem() instanceof AxeItem)) continue;
            int v = sc(q.getItem());
            if (v > s) {
                s = v;
                b = i;
            }
        }
        return b;
    }

    private int sc(Item i) {
        if (i == Items.NETHERITE_AXE) return 60;
        if (i == Items.DIAMOND_AXE) return 50;
        if (i == Items.IRON_AXE) return 40;
        if (i == Items.STONE_AXE) return 30;
        if (i == Items.GOLDEN_AXE) return 20;
        if (i == Items.WOODEN_AXE) return 10;
        return 5;
    }

    private boolean fc(Player p) {
        Vec3 d = new Vec3(mc.player.getEyePosition().x - p.getEyePosition().x, 0.0, mc.player.getEyePosition().z - p.getEyePosition().z);
        if (d.lengthSqr() < 1.0e-6) return true;
        d = d.normalize();
        Vec3 f = p.calculateViewVector(0.0F, p.getYHeadRot());
        Vec3 g = new Vec3(f.x, 0.0, f.z);
        if (g.lengthSqr() < 1.0e-6) return true;
        g = g.normalize();
        return g.dot(d) >= 0.0;
    }

    public static boolean blocking(LivingEntity t) {
        if (t == null) return false;
        if (t.isBlocking()) return true;
        if (!t.isUsingItem()) return false;
        return !t.getUseItem().isEmpty() && t.getUseItem().getItem() instanceof ShieldItem;
    }

    public static boolean busy() {
        if (Raze.featureManager == null) return false;
        ShieldStun s = Raze.featureManager.getFeatureByClass(ShieldStun.class);
        return s != null && s.dl != 0;
    }

    public static boolean owns(LivingEntity t) {
        if (Raze.featureManager == null) return false;
        ShieldStun s = Raze.featureManager.getFeatureByClass(ShieldStun.class);
        if (s == null || !s.isEnabled()) return false;
        if (!(t instanceof Player p)) return false;
        return p.isAlive() && blocking(p) && s.fc(p);
    }
}
