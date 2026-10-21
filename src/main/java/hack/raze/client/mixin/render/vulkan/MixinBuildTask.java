package hack.raze.client.mixin.render.vulkan;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.vulkanmod.render.chunk.build.task.BuildTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BuildTask.class, remap = false)
public class MixinBuildTask {

    @Redirect(
        method = "compile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;",
            remap = true
        )
    )
    private RenderShape filterBlockRenderShape(BlockState blockState) {
        return blockState.getRenderShape();
    }
}
