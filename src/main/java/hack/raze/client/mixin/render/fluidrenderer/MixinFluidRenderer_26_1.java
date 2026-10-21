package hack.raze.client.mixin.render.fluidrenderer;

//? if >=26.1 {
/*import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public class MixinFluidRenderer_26_1 {

    @Inject(method = "getLightCoords", at = @At("RETURN"), cancellable = true)
    private void raze$forceBrightLiquids(BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
    }

    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void raze$skipFluids(
            BlockAndTintGetter level,
            BlockPos pos,
            FluidRenderer.Output output,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci
    ) {
    }
}
*///?} else {
public final class MixinFluidRenderer_26_1 {
}
//?}
