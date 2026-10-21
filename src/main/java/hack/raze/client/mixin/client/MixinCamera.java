package hack.raze.client.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Camera.class)
public abstract class MixinCamera {

    @Shadow
    protected abstract void setPosition(Vec3 vec3);
    @Shadow
    protected abstract void setRotation(float f, float g);



    @Inject(method = "setup", at = @At("RETURN"), cancellable = true)
    private void raze$onSetup(Level level, Entity entity, boolean bl, boolean bl2, float f, CallbackInfo ci) {
    }
}
