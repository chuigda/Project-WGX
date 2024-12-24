package chr.wgx.render.vk.compiled;

import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.task.VulkanRenderPipelineBind;
import tech.icey.vk4j.enumtype.VkPipelineBindPoint;
import tech.icey.vk4j.handle.VkCommandBuffer;

public final class RenderOp implements CompiledRenderPassOp {
    public RenderOp(VulkanRenderPipelineBind bindPoint) {
        this.bindPoint = bindPoint;
    }

    @Override
    public void recordToCommandBuffer(
            VulkanRenderEngineContext cx,
            Swapchain swapchain,
            VkCommandBuffer cmdBuf,
            int frameIndex
    ) {
        cx.dCmd.vkCmdBindPipeline(
                cmdBuf,
                VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                bindPoint.pipeline.pipeline
        );
    }

    private final VulkanRenderPipelineBind bindPoint;
}
