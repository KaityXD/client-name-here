package hack.raze.client.mixin.render.sodium.versioned;

//? if <26.1 {
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockRenderer.class, remap = false)
public class MixinBlockRenderer_1_21_11 {

    @Inject(method = "renderModel", at = @At("HEAD"), cancellable = true)
    private void filterBlockRender(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
    }
}
//?} else {
/*public final class MixinBlockRenderer_1_21_11 {
}
*///?}
