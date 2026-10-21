package hack.raze.client.mixin.input;

import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventInput;
import hack.raze.client.event.impl.EventMovementInput;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class MixinKeyboardInput extends ClientInput {

    @Shadow
    @Final
    private Options options;

    @Unique private boolean originalForward;
    @Unique private boolean originalBack;
    @Unique private boolean originalLeft;
    @Unique private boolean originalRight;
    @Unique private boolean originalJump;
    @Unique private boolean originalSneak;
    @Unique private boolean originalSprint;

    @Unique private EventMovementInput raze$moveEvent;

    @Unique
    private void raze$setKeyState(KeyMapping key, boolean desired, boolean toggleEnabled) {
        if (key instanceof ToggleKeyMapping && toggleEnabled) {
            if (key.isDown() != desired) {
                key.setDown(true);
            }
            return;
        }

        key.setDown(desired);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        if (Raze.isDestroyed) {
            this.raze$moveEvent = null;
            return;
        }

        EventInput event = new EventInput(this);
        event.call();

        this.originalForward = this.options.keyUp.isDown();
        this.originalBack = this.options.keyDown.isDown();
        this.originalLeft = this.options.keyLeft.isDown();
        this.originalRight = this.options.keyRight.isDown();
        this.originalJump = this.options.keyJump.isDown();
        this.originalSneak = this.options.keyShift.isDown();
        this.originalSprint = this.options.keySprint.isDown();

        raze$moveEvent = new EventMovementInput(
            this.originalForward,
            this.originalBack,
            this.originalLeft,
            this.originalRight,
            this.originalJump,
            this.originalSneak,
            this.originalSprint
        );

        raze$moveEvent.call();

        raze$moveEvent.syncFloatsFromBooleans();

        this.options.keyUp.setDown(raze$moveEvent.forwardAmount > 0);
        this.options.keyDown.setDown(raze$moveEvent.forwardAmount < 0);
        this.options.keyLeft.setDown(raze$moveEvent.sidewaysAmount > 0);
        this.options.keyRight.setDown(raze$moveEvent.sidewaysAmount < 0);
        this.options.keyJump.setDown(raze$moveEvent.jump);
        this.raze$setKeyState(this.options.keyShift, raze$moveEvent.sneak, this.options.toggleCrouch().get());
        this.raze$setKeyState(this.options.keySprint, raze$moveEvent.sprint, this.options.toggleSprint().get());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTickPost(CallbackInfo ci) {
        if (Raze.isDestroyed) {
            this.raze$moveEvent = null;
            return;
        }

        this.options.keyUp.setDown(this.originalForward);
        this.options.keyDown.setDown(this.originalBack);
        this.options.keyLeft.setDown(this.originalLeft);
        this.options.keyRight.setDown(this.originalRight);
        this.options.keyJump.setDown(this.originalJump);
        this.raze$setKeyState(this.options.keyShift, this.originalSneak, this.options.toggleCrouch().get());
        this.raze$setKeyState(this.options.keySprint, this.originalSprint, this.options.toggleSprint().get());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void raze$onTickFreecam(CallbackInfo ci) {
    }
}
