package hack.raze.client.mixin.input;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientInput.class)
public class MixinClientInput {

    @Shadow
    protected Vec2 moveVector;

    @Inject(method = "hasForwardImpulse", at = @At("HEAD"), cancellable = true)
    private void onHasForwardImpulse2(CallbackInfoReturnable<Boolean> cir) {
    }
}