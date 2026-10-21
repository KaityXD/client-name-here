package hack.raze.client.mixin.render.indigo.versioned;

//? if <26.1 {
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractTerrainRenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractTerrainRenderContext.class, remap = false)
public class MixinAbstractTerrainRenderContextLight_1_21_11 {

    @Shadow @Final protected BlockRenderInfo blockInfo;

    @ModifyVariable(method = "shadeQuad", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean raze$disableAoForXray(boolean ao) {
        return ao;
    }

    @Inject(method = "shadeQuad", at = @At("TAIL"))
    private void raze$forceFullbrightForXray(MutableQuadViewImpl quad, boolean ao, boolean emissive, boolean vanillaShade, CallbackInfo ci) {
    }
}
//?} else {
/*public final class MixinAbstractTerrainRenderContextLight_1_21_11 {
}
*///?}
