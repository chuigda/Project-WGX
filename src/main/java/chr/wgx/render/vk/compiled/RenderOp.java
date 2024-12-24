package chr.wgx.render.vk.compiled;

import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.task.VulkanRenderRenderPipelineBind;
import chr.wgx.render.vk.task.VulkanRenderTask;
import chr.wgx.render.vk.task.VulkanRenderTaskGroup;
import tech.icey.vk4j.enumtype.VkIndexType;
import tech.icey.vk4j.enumtype.VkPipelineBindPoint;
import tech.icey.vk4j.handle.VkCommandBuffer;

public final class RenderOp implements CompiledRenderPassOp {
    public RenderOp(VulkanRenderRenderPipelineBind bindPoint) {
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

        for (VulkanRenderTaskGroup renderTaskGroup : bindPoint.renderTaskGroups) {
            if (!renderTaskGroup.isEnabled()) {
                continue;
            }

            if (!renderTaskGroup.sharedDescriptorSets.isEmpty()) {
                cx.dCmd.vkCmdBindDescriptorSets(
                        cmdBuf,
                        VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                        bindPoint.pipeline.pipelineLayout,
                        0,
                        renderTaskGroup.sharedDescriptorSets.size(),
                        renderTaskGroup.sharedDescriptorSetsVk,
                        0,
                        null
                );
            }

            for (VulkanRenderTask renderTask : renderTaskGroup.renderTasks) {
                if (!renderTask.isEnabled()) {
                    continue;
                }

                if (!renderTask.descriptorSets.isEmpty()) {
                    cx.dCmd.vkCmdBindDescriptorSets(
                            cmdBuf,
                            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                            bindPoint.pipeline.pipelineLayout,
                            renderTaskGroup.sharedDescriptorSets.size(),
                            renderTask.descriptorSets.size(),
                            renderTask.descriptorSetsVk,
                            0,
                            null
                    );
                }

                cx.dCmd.vkCmdBindVertexBuffers(cmdBuf, 0, 1, renderTask.pBuffer, renderTask.pOffsets);
                cx.dCmd.vkCmdBindIndexBuffer(
                        cmdBuf,
                        renderTask.renderObject.indexBuffer.buffer,
                        0,
                        VkIndexType.VK_INDEX_TYPE_UINT32
                );
                cx.dCmd.vkCmdDrawIndexed(
                        cmdBuf,
                        renderTask.renderObject.indexCount,
                        1,
                        0,
                        0,
                        0
                );
            }
        }
    }

    private final VulkanRenderRenderPipelineBind bindPoint;
}
