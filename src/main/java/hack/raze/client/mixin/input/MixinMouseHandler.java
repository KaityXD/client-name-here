package hack.raze.client.mixin.input;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import hack.raze.client.Raze;
import hack.raze.client.event.impl.MousePressEvent;
import hack.raze.client.event.impl.MouseUpdateEvent;

@Mixin(MouseHandler.class)
public class MixinMouseHandler {

    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onMouseUpdatePre(CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        MouseUpdateEvent event = new MouseUpdateEvent(accumulatedDX, accumulatedDY);
        event.call();
        if (event.cancelled) ci.cancel();
    }

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        MousePressEvent event = new MousePressEvent(input.button(), action);
        event.call();
        if (event.cancelled) ci.cancel();
    }

    @Shadow
    private double xpos;
    @Shadow
    private double ypos;

    public void setCursorPosition(double newX, double newY) {
        this.xpos = newX;
        this.ypos = newY;
    }
}
