package hack.raze.client.mixin.render.vulkan.versioned;

//? if <=26.1.2 {
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.vulkanmod.render.chunk.build.renderer.FluidRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidRenderer.class, remap = false)
public class MixinFluidRenderer_1_21_11 {

    @Inject(method = "renderLiquid", at = @At("HEAD"), cancellable = true)
    private void cancelFluidRender(BlockState blockState, FluidState fluidState, BlockPos blockPos, CallbackInfo ci) {
    }
}
//?} else {
/*public final class MixinFluidRenderer_1_21_11 {
}
*///?}
