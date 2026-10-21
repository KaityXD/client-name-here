package hack.raze.client.mixin.input;

import hack.raze.client.Raze;
import hack.raze.client.event.impl.SprintEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class MixinKeymapping {

    @Shadow private boolean isDown;

    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void onIsDown(CallbackInfoReturnable<Boolean> cir) {
        if (Raze.isDestroyed) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null || mc.player == null) return;
        
        if ((Object) this == mc.options.keySprint) {
            SprintEvent event = new SprintEvent(this.isDown);
            event.call();
            if (event.isOverridden()) {
                cir.setReturnValue(event.shouldSprint());
            }
        }
    }
}
