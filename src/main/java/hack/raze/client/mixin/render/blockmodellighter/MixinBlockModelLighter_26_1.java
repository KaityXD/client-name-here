package hack.raze.client.mixin.render.blockmodellighter;

//? if >=26.1 {
/*import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelLighter.class)
public class MixinBlockModelLighter_26_1 {

    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("TAIL"))
    private void raze$forceWhiteAmbient(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos centerPosition,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
    }

    @Inject(method = "prepareQuadFlat", at = @At("TAIL"))
    private void raze$forceWhiteFlat(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            int lightCoords,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
    }
}
*///?} else {
public final class MixinBlockModelLighter_26_1 {
}
//?}
