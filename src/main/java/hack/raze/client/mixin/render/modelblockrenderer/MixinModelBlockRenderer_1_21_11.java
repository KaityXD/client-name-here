package hack.raze.client.mixin.render.modelblockrenderer;

//? if <26.1 {
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.block.ModelBlockRenderer$Cache")
public class MixinModelBlockRenderer_1_21_11 {

    @Inject(method = "getLightColor", at = @At("RETURN"), cancellable = true)
    private void modifyLightColor(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, CallbackInfoReturnable<Integer> cir) {
    }
}
//?} else {
/*public final class MixinModelBlockRenderer_1_21_11 {
}
*///?}
