package hack.raze.client.vulkan.utils;

import hack.raze.client.utils.Imports;
import hack.raze.client.vulkan.memory.TransferQueue;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

public interface VkContext extends Imports {

    TransferQueue getTransferQueue();
    VkDevice getDevice();
    VkCommandBuffer getCommandBuffer();
    Vector4i getScissorRect(float x, float y, float w, float h);
    long getVma();
    int getCurrentFrame();
    int getFramesNum();
    boolean isDynamicRendering();

    float depthClearValue();
    int depthCompareOp();

}
