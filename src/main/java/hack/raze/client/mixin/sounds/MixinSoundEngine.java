package hack.raze.client.mixin.sounds;

import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventPlaySound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import hack.raze.client.utils.audio.RazeAudio;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {
    @Inject(method = "reload()V", at = @At("TAIL"))
    private void onReload(CallbackInfo ci) {
        if (Raze.isDestroyed) return;
        RazeAudio.reinitBuffers();
    }

    @Inject(method = "tick(Z)V", at = @At("HEAD"))
    private void onTick(boolean paused, CallbackInfo ci) {
        if (Raze.isDestroyed) return;
        RazeAudio.tick();
    }

    @Inject(
        method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/resources/sounds/SoundInstance;resolve(Lnet/minecraft/client/sounds/SoundManager;)Lnet/minecraft/client/sounds/WeighedSoundEvents;",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void onPlaySound(SoundInstance soundInstance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (Raze.isDestroyed) return;
        
        if (soundInstance.getSound() == null) {
            return;
        }
        
        Identifier identifier = soundInstance.getIdentifier();
        BuiltInRegistries.SOUND_EVENT.getOptional(identifier).ifPresent(soundEvent -> {
            try {
                EventPlaySound soundEventWrapper = new EventPlaySound(
                    soundInstance.getX(),
                    soundInstance.getY(),
                    soundInstance.getZ(),
                    soundEvent,
                    soundInstance.getSource(),
                    soundInstance.getVolume(),
                    soundInstance.getPitch()
                );
                soundEventWrapper.call();
                if (soundEventWrapper.cancelled) {
                    cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
                }
            } catch (Exception e) {
            }
        });
    }
}