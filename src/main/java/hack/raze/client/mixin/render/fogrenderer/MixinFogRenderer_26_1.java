package hack.raze.client.mixin.render.fogrenderer;

//? if >=26.1 {
/*import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer_26_1 {

    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private void raze$modifyFogState(Camera camera, int viewDistance, DeltaTracker tickCounter, float skyDarkness, ClientLevel world, CallbackInfoReturnable<FogData> cir) {
    }
}
*///?} else {
public final class MixinFogRenderer_26_1 {
}
//?}
