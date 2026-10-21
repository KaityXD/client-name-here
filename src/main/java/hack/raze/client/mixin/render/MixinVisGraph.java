package hack.raze.client.mixin.render;

import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventChunkOcclusion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VisGraph.class)
public abstract class MixinVisGraph {

    @Inject(method = "setOpaque", at = @At("HEAD"), cancellable = true)
    private void raze$onMarkClosed(BlockPos pos, CallbackInfo ci) {
        if (Raze.isDestroyed) return;

        EventChunkOcclusion event = new EventChunkOcclusion();
        event.call();
        if (event.cancelled) {
            ci.cancel();
        }
    }
}
