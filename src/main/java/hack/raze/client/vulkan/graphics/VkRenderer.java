package hack.raze.client.vulkan.graphics;

import hack.raze.client.vulkan.CommandBuffer;

public interface VkRenderer {
    void beginFrame();
    void flush(CommandBuffer commandBuffer);
    void cleanup();
}
