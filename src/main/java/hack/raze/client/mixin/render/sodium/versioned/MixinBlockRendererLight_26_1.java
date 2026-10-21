package hack.raze.client.mixin.render.sodium.versioned;

//? if >=26.1 {
/*import net.caffeinemc.mods.sodium.client.model.light.LightMode;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.render.model.SodiumShadeMode;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public class MixinBlockRendererLight_26_1 {

    @Shadow
    private BlockState state;

    @Shadow
    protected QuadLightData quadLightData;

    @Inject(method = "shadeQuad", at = @At("TAIL"))
    private void raze$modifyLight(
            MutableQuadViewImpl quad,
            LightMode lightMode,
            boolean emissive,
            SodiumShadeMode shadeMode,
            CallbackInfo ci
    ) {
    }
}
*///?} else {
public final class MixinBlockRendererLight_26_1 {
}
//?}
