package chr.wgx.render.vk.compiled;

import chr.wgx.render.info.PushConstantRange;
import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanPushConstant;
import chr.wgx.render.vk.task.VulkanRenderPipelineBind;
import chr.wgx.render.vk.task.VulkanRenderTask;
import chr.wgx.render.vk.task.VulkanRenderTaskDynamic;
import chr.wgx.render.vk.task.VulkanRenderTaskGroup;
import tech.icey.panama.buffer.LongBuffer;
import tech.icey.vk4j.enumtype.VkIndexType;
import tech.icey.vk4j.enumtype.VkPipelineBindPoint;
import tech.icey.vk4j.handle.VkBuffer;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.vk4j.handle.VkDescriptorSet;
import tech.icey.vk4j.handle.VkPipelineLayout;
import tech.icey.xjbutil.container.Option;

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

        for (VulkanRenderTaskGroup renderTaskGroup : bindPoint.renderTaskGroups) {
            if (!renderTaskGroup.isEnabled()) {
                continue;
            }

            int sharedDescriptorCount = renderTaskGroup.sharedDescriptorSets.size();
            if (sharedDescriptorCount != 0) {
                VkDescriptorSet.Buffer pDescriptorSetVk;
                if (renderTaskGroup.sharedDescriptorSets.size() == 1) {
                    pDescriptorSetVk = renderTaskGroup.sharedDescriptorSetsVk[0];
                } else {
                    pDescriptorSetVk = renderTaskGroup.sharedDescriptorSetsVk[frameIndex];
                }

                cx.dCmd.vkCmdBindDescriptorSets(
                        cmdBuf,
                        VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                        bindPoint.pipeline.pipelineLayout,
                        0,
                        sharedDescriptorCount,
                        pDescriptorSetVk,
                        0,
                        null
                );
            }

            for (VulkanRenderTask renderTask : renderTaskGroup.renderTasks) {
                if (!renderTask.isEnabled()) {
                    continue;
                }

                VkDescriptorSet.Buffer pDescriptorSetsVk;
                if (renderTask.descriptorSetsVk.length == 1) {
                    pDescriptorSetsVk = renderTask.descriptorSetsVk[0];
                } else {
                    pDescriptorSetsVk = renderTask.descriptorSetsVk[frameIndex];
                }

                if (!renderTask.descriptorSets.isEmpty()) {
                    cx.dCmd.vkCmdBindDescriptorSets(
                            cmdBuf,
                            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                            bindPoint.pipeline.pipelineLayout,
                            sharedDescriptorCount,
                            renderTask.descriptorSets.size(),
                            pDescriptorSetsVk,
                            0,
                            null
                    );
                }

                applyPushConstant(cx, cmdBuf, bindPoint.pipeline.pipelineLayout, renderTask.pushConstant);
                recordDrawCommand(
                        cx,
                        cmdBuf,
                        renderTask.pBuffer,
                        renderTask.pOffsets,
                        renderTask.renderObject.indexBuffer.buffer,
                        renderTask.renderObject.indexCount
                );
            }

            for (VulkanRenderTaskDynamic dynamicRenderTask : renderTaskGroup.dynamicRenderTasks) {
                if (!renderTaskGroup.isEnabled()) {
                    continue;
                }

                if (!dynamicRenderTask.descriptorSets.isEmpty()) {
                    VkDescriptorSet.Buffer pDescriptorSetsVk = dynamicRenderTask.descriptorSetsVk;
                    for (int i = 0; i < dynamicRenderTask.descriptorSets.size(); i++) {
                        VulkanDescriptorSet descriptorSet = dynamicRenderTask.descriptorSets.get(i).get();
                        if (descriptorSet.descriptorSets.length == 1) {
                            pDescriptorSetsVk.write(i, descriptorSet.descriptorSets[0]);
                        } else {
                            pDescriptorSetsVk.write(i, descriptorSet.descriptorSets[frameIndex]);
                        }
                    }

                    cx.dCmd.vkCmdBindDescriptorSets(
                            cmdBuf,
                            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                            bindPoint.pipeline.pipelineLayout,
                            renderTaskGroup.sharedDescriptorSets.size(),
                            dynamicRenderTask.descriptorSets.size(),
                            pDescriptorSetsVk,
                            0,
                            null
                    );
                }

                applyPushConstant(cx, cmdBuf, bindPoint.pipeline.pipelineLayout, dynamicRenderTask.pushConstant);
                recordDrawCommand(
                        cx,
                        cmdBuf,
                        dynamicRenderTask.pBuffer,
                        dynamicRenderTask.pOffsets,
                        dynamicRenderTask.renderObject.indexBuffer.buffer,
                        dynamicRenderTask.renderObject.indexCount
                );
            }
        }
    }

    private static void applyPushConstant(
            VulkanRenderEngineContext cx,
            VkCommandBuffer cmdBuf,
            VkPipelineLayout pipelineLayout,
            Option<VulkanPushConstant> pushConstantOption
    ) {
        if (pushConstantOption instanceof Option.Some<VulkanPushConstant> some) {
            VulkanPushConstant pushConstant = some.value;
            synchronized (pushConstant) {
                for (PushConstantRange range : pushConstant.info.pushConstantRanges) {
                    cx.dCmd.vkCmdPushConstants(
                            cmdBuf,
                            pipelineLayout,
                            range.shaderStage.vkShaderStageFlags,
                            range.offset,
                            range.type.byteSize,
                            pushConstant.segment.asSlice(range.offset, range.type.byteSize)
                    );
                }
            }
        }
    }

    private static void recordDrawCommand(
            VulkanRenderEngineContext cx,
            VkCommandBuffer cmdBuf,
            VkBuffer.Buffer pVertexBuffer,
            LongBuffer pOffsets,
            VkBuffer indexBuffer,
            int indexCount
    ) {
        cx.dCmd.vkCmdBindVertexBuffers(cmdBuf, 0, 1, pVertexBuffer, pOffsets);
        cx.dCmd.vkCmdBindIndexBuffer(
                cmdBuf,
                indexBuffer,
                0,
                VkIndexType.VK_INDEX_TYPE_UINT32
        );
        cx.dCmd.vkCmdDrawIndexed(
                cmdBuf,
                indexCount,
                1,
                0,
                0,
                0
        );
    }

    private final VulkanRenderPipelineBind bindPoint;
}
