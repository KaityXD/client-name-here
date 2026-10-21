package hack.raze.client.mixin.render.modelblockrenderer;

//? if >=26.1 {
/*import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBlockRenderer.class)
public class MixinModelBlockRenderer_26_1 {

    @Shadow
    private boolean shouldRenderFace(BlockAndTintGetter level, BlockState state, Direction direction, BlockPos neighborPos) {
        throw new AssertionError();
    }

    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    private void raze$skipHiddenBlocks(
            BlockQuadOutput output,
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

    @Redirect(
            method = "tesselateAmbientOcclusion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;shouldRenderFace(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean raze$alwaysRenderSelectedBlockFacesAmbient(
            ModelBlockRenderer instance,
            BlockAndTintGetter level,
            BlockState blockState,
            Direction direction,
            BlockPos neighborPos
    ) {
        return this.shouldRenderFace(level, blockState, direction, neighborPos);
    }

    @Redirect(
            method = "tesselateFlat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;shouldRenderFace(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean raze$alwaysRenderSelectedBlockFacesFlat(
            ModelBlockRenderer instance,
            BlockAndTintGetter level,
            BlockState blockState,
            Direction direction,
            BlockPos neighborPos
    ) {
        return this.shouldRenderFace(level, blockState, direction, neighborPos);
    }
}
*///?} else {
public final class MixinModelBlockRenderer_26_1 {
}
//?}
