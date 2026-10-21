package hack.raze.client.event.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import hack.raze.client.event.Event;
import hack.raze.client.api.GuiGraphicsCompat;
import hack.raze.client.render2.api.CrossTexture;
import hack.raze.client.render2.api.Draw2D;
import hack.raze.client.render2.impl.opengl.utils.RenderUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class EventRender2DGui extends Event {
    private PoseStack matrixStack;
    private float tickDelta;
    private GuiGraphicsCompat context;
    private Draw2D draw2D;
    private CrossTexture blurTexture;

    public int getScaledWidth() {
        return RenderUtil.getScaledWidth();
    }

    public int getScaledHeight() {
        return RenderUtil.getScaledHeight();
    }
}
