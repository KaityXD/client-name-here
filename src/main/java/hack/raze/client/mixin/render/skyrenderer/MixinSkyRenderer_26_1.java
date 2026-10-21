package hack.raze.client.mixin.render.skyrenderer;

//? if >=26.1 {
/*import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class MixinSkyRenderer_26_1 {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void raze$applyAmbienceSkyColor(ClientLevel world, float tickProgress, Camera camera, SkyRenderState state, CallbackInfo ci) {
    }
}
*///?} else {
public final class MixinSkyRenderer_26_1 {
}
//?}
