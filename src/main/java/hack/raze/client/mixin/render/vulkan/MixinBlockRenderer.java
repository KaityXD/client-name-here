package hack.raze.client.mixin.render.vulkan;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.vulkanmod.render.chunk.build.light.data.QuadLightData;
import net.vulkanmod.render.chunk.build.renderer.BlockRenderer;
import net.vulkanmod.render.model.quad.ModelQuadView;
import net.vulkanmod.render.vertex.TerrainBuilder;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class MixinBlockRenderer {

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void filterBlockRender(BlockState state, BlockPos pos, Vector3f origin, CallbackInfo ci) {
    }

    @ModifyArg(
            method = "renderBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;emitQuads(Lnet/fabricmc/fabric/api/client/renderer/v1/mesh/QuadEmitter;Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Ljava/util/function/Predicate;)V",
                    remap = false
            ),
            index = 5,
            require = 0
    )
    private Predicate<?> raze$showAllSelectedBlockFaces_26_1(Predicate<?> cullTest) {
        return cullTest;
    }

    @ModifyArg(
            method = "renderBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_1087;emitQuads(Lnet/fabricmc/fabric/api/renderer/v1/mesh/QuadEmitter;Lnet/minecraft/class_1920;Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Lnet/minecraft/class_5819;Ljava/util/function/Predicate;)V",
                    remap = false
            ),
            index = 5,
            require = 0
    )
    private Predicate<?> raze$showAllSelectedBlockFaces_1_21_11(Predicate<?> cullTest) {
        return cullTest;
    }

    @Redirect(
            method = "endRenderQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/render/chunk/build/renderer/BlockRenderer;bufferQuad(Lnet/vulkanmod/render/vertex/TerrainBuilder;Lorg/joml/Vector3f;Lnet/vulkanmod/render/model/quad/ModelQuadView;Lnet/vulkanmod/render/chunk/build/light/data/QuadLightData;)V"
            )
    )
    private void raze$forceFullbrightForXray(
            BlockRenderer instance,
            TerrainBuilder terrainBuilder,
            Vector3f pos,
            ModelQuadView quad,
            QuadLightData quadLightData
    ) {
        instance.bufferQuad(terrainBuilder, pos, quad, quadLightData);
    }

}
