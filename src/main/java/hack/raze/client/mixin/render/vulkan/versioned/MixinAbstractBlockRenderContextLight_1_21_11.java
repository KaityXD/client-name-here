package hack.raze.client.mixin.render.vulkan.versioned;

//? if <=26.1.2 {
import net.minecraft.world.level.block.state.BlockState;
import net.vulkanmod.render.chunk.build.frapi.mesh.MutableQuadViewImpl;
import net.vulkanmod.render.chunk.build.frapi.render.AbstractBlockRenderContext;
import net.vulkanmod.render.chunk.build.light.LightPipeline;
import net.vulkanmod.render.chunk.build.light.data.QuadLightData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class MixinAbstractBlockRenderContextLight_1_21_11 {

    @Shadow protected BlockState blockState;
    @Shadow @Final protected QuadLightData quadLightData;

    @Inject(
            method = "shadeQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/render/chunk/build/light/LightPipeline;calculate(Lnet/vulkanmod/render/model/quad/ModelQuadView;Lnet/minecraft/core/BlockPos;Lnet/vulkanmod/render/chunk/build/light/data/QuadLightData;Lnet/minecraft/core/Direction;Lnet/minecraft/core/Direction;Z)V",
                    shift = At.Shift.AFTER
            )
    )
    private void modifyLightForXray(MutableQuadViewImpl quad, LightPipeline lightPipeline, boolean useQuadColorData, boolean ao, CallbackInfo ci) {
    }
}
//?} else {
/*public final class MixinAbstractBlockRenderContextLight_1_21_11 {
}
*///?}
