package hack.raze.client.mixin.render.fogrenderer;

//? if <26.1 {
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FogRenderer.class)
public class MixinFogRenderer_1_21_11 {

    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private void raze$modifyFogColor(Camera camera, int viewDistance, DeltaTracker tickCounter, float skyDarkness, ClientLevel world, CallbackInfoReturnable<Vector4f> cir) {
    }

    @ModifyArgs(
            method = "setupFog",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"
            )
    )
    private void raze$modifyFogDistances(Args args) {
    }
}
//?} else {
/*public final class MixinFogRenderer_1_21_11 {
}
*///?}
