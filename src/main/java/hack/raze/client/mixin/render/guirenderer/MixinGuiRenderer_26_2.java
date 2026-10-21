package hack.raze.client.mixin.render.guirenderer;

//? if >26.1.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vulkan.VulkanRenderPass;
import hack.raze.client.Raze;
import hack.raze.client.api.GuiGraphicsCompat;
import hack.raze.client.api.MinecraftCompat;
import hack.raze.client.api.ScreenRenderCompat;
import hack.raze.client.event.impl.EventRender2DGui;
import hack.raze.client.mixin.accessors.RenderPassAccessor;
import hack.raze.client.render2.api.CrossTexture;
import hack.raze.client.render2.impl.opengl.utils.RenderUtil;
import hack.raze.client.render2.impl.vulkan.api.VkDraw2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static hack.raze.client.utils.Imports.mc;

@Mixin(GuiRenderer.class)
public class MixinGuiRenderer_26_2 {

    @Shadow
    @Final
    private GuiRenderState renderState;

    private GuiRenderState echoRenderState;
    private GuiGraphicsExtractor echoGraphics;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;prepare()V"
            )
    )
    private void raze$renderGui2D(CallbackInfo ci) {
        if (Raze.isDestroyed || Raze.draw2D == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        if (MinecraftCompat.getScreen() instanceof ScreenRenderCompat) {
            int mouseX = (int) minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
            int mouseY = (int) minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
            GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(minecraft, renderState, mouseX, mouseY);

            var draw = Raze.draw2D;
            draw.beginFrame(RenderUtil.getProjectionMatrix());
            CrossTexture blurTexture = draw.getBlurResult();

            EventRender2DGui event = new EventRender2DGui(
                    new PoseStack(),
                    minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true),
                    GuiGraphicsCompat.of(graphics),
                    draw,
                    blurTexture
            );
            event.call();
            draw.endFrame();
            return;
        }

        echoRenderState = new GuiRenderState();
        int mouseX = (int) minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
        int mouseY = (int) minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
        echoGraphics = new GuiGraphicsExtractor(minecraft, echoRenderState, mouseX, mouseY);

        var draw = Raze.draw2D;


        draw.beginFrame(RenderUtil.getProjectionMatrix());
        CrossTexture blurTexture = draw.getBlurResult();

        EventRender2DGui event = new EventRender2DGui(
                new PoseStack(),
                minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true),
                GuiGraphicsCompat.of(echoGraphics),
                draw,
                blurTexture
        );
        event.call();
        draw.endFrame();
    }

    @Inject(
            method = "prepare()V",
            at = @At("HEAD")
    )
    private void raze$injectStratum(CallbackInfo ci) {
        if (Raze.isDestroyed) {
            echoRenderState = null;
            echoGraphics = null;
            return;
        }

        if (echoRenderState == null || echoRenderState.strata.isEmpty()) {
            return;
        }

        var echoNode = echoRenderState.strata.get(0);
        renderState.strata.add(1, echoNode);
        renderState.itemModelIdentities.addAll(echoRenderState.itemModelIdentities);

        echoRenderState = null;
        echoGraphics = null;
    }

}
*///?} else {
public final class MixinGuiRenderer_26_2 {
}
//?}