package hack.raze.client.mixin.render.vulkan;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.vulkanmod.render.chunk.build.frapi.render.AbstractBlockRenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class MixinAbstractBlockRenderContextOcclusion {

    @Inject(method = "faceNotOccluded", at = @At("HEAD"), cancellable = true)
    private void overrideOcclusionForXrayBlocks(BlockState selfBlockState, Direction facing, CallbackInfoReturnable<Boolean> cir) {
    }
}
