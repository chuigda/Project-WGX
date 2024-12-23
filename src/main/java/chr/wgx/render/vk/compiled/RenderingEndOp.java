package chr.wgx.render.vk.compiled;

import chr.wgx.render.vk.VulkanRenderEngineContext;
import tech.icey.vk4j.handle.VkCommandBuffer;

public final class RenderingEndOp implements CompiledRenderPassOp {
    @Override
    public void recordToCommandBuffer(VulkanRenderEngineContext cx, VkCommandBuffer cmdBuf, int frameIndex) {
        cx.dCmd.vkCmdEndRendering(cmdBuf);
    }
}
