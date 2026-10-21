package hack.raze.client.mixin.render;

import hack.raze.client.Raze;
import hack.raze.client.event.impl.EventChunkOcclusion;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SectionOcclusionGraph.class)
public class MixinSectionOcclusionGraph {

    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private boolean raze$disableSmartCull(boolean smartCull) {
        if (Raze.isDestroyed) return smartCull;
        if (!smartCull) return false;
        EventChunkOcclusion event = new EventChunkOcclusion();
        event.call();
        return event.cancelled ? false : smartCull;
    }
}
