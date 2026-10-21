package hack.raze.client.mixin.render.indigo.versioned;

//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltModelBlockRendererImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AltModelBlockRendererImpl.class, remap = false)
public class MixinAltModelBlockRendererImpl_26_1 {

    @Shadow
    private BlockState blockState;

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void raze$hideNonSelectedBlocksForXray(
            QuadEmitter output,
            float x,
            float y,
            float z,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState blockState,
            BlockStateModel model,
            long seed,
            CallbackInfo ci
    ) {
    }

    @Inject(method = "shouldCullFace", at = @At("HEAD"), cancellable = true)
    private void raze$drawSelectedBlockFacesAgainstHiddenNeighbors(Direction direction, CallbackInfoReturnable<Boolean> cir) {
    }

    @ModifyVariable(method = "shadeQuad", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean raze$disableAoForXray(boolean ao) {
        return ao;
    }

    @Inject(method = "shadeQuad", at = @At("TAIL"))
    private void raze$forceFullbrightForXray(
            MutableQuadViewImpl quad,
            boolean ao,
            boolean emissive,
            boolean vanillaShade,
            CallbackInfo ci
    ) {
    }
}
*///?} else {
public final class MixinAltModelBlockRendererImpl_26_1 {
}
//?}
