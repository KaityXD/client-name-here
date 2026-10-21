package hack.raze.client.vulkan.compute;

import hack.raze.client.vulkan.CommandBuffer;

public interface ComputeDispatch {
    void executeCompute(CommandBuffer cmd);
    void cleanup();
}
