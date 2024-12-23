package chr.wgx.render.vk.compiled;

import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import tech.icey.vk4j.handle.VkCommandBuffer;

public sealed interface CompiledRenderPassOp permits
        ImageBarrierOp,
        RenderingBeginOp,
        RenderingEndOp,
        RenderOp
{
    void recordToCommandBuffer(
            VulkanRenderEngineContext cx,
            Swapchain swapchain,
            VkCommandBuffer cmdBuf,
            int frameIndex
    );
}
