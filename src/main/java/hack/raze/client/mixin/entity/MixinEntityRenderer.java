package hack.raze.client.mixin.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> {
    
    @Inject(method = "extractRenderState", at = @At("TAIL"), cancellable = true)
    private void raze$forcePlayerNameTag(T entity, S entityRenderState, float f, CallbackInfo ci) {
    }
}
