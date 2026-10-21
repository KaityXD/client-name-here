package hack.raze.client.mixin.render.sodium.versioned;

//? if >=26.1 {
/*import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadViewMutable;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DefaultFluidRenderer.class, remap = false)
public class MixinDefaultFluidRenderer_26_1 {

    @Shadow
    private QuadLightData quadLightData;

    @Inject(method = "updateQuad", at = @At("TAIL"))
    private void raze$modifyFluidLight(
            ModelQuadViewMutable quad,
            LevelSlice level,
            BlockPos pos,
            LightPipeline lighter,
            Direction dir,
            ModelQuadFacing facing,
            float brightness,
            ColorProvider<FluidState> colorProvider,
            FluidState fluidState,
            CallbackInfo ci
    ) {
    }
}
*///?} else {
public final class MixinDefaultFluidRenderer_26_1 {
}
//?}
