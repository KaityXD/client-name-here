package hack.raze.client.mixin.render;

import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SectionCompiler.class)
public class MixinSectionCompiler {

    @Redirect(
        method = "compile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;isSolidRender()Z"
        )
    )
    private boolean raze$disableHiddenBlockOcclusion(BlockState blockState) {
        return blockState.isSolidRender();
    }
}
