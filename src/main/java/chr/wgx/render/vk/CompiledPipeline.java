package chr.wgx.render.vk;

import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.VkPipelineBindPoint;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.vk4j.handle.VkDescriptorSet;
import tech.icey.vk4j.handle.VkPipeline;
import tech.icey.vk4j.handle.VkPipelineLayout;

public final class CompiledPipeline {
    private sealed interface CompiledPipelineComponent {
        void toCommandBuffer(VulkanRenderEngine engine, VkCommandBuffer commandBuffer);
    }

    private static final class ImageMemoryBarrier implements CompiledPipelineComponent {
        private final VkImageMemoryBarrier[] barriers;
        private final @enumtype(VkPipelineStageFlags.class) int srcStageMask;
        private final @enumtype(VkPipelineStageFlags.class) int dstStageMask;

        private ImageMemoryBarrier(
                VkImageMemoryBarrier[] barriers,
                @enumtype(VkPipelineStageFlags.class) int srcStageMask,
                @enumtype(VkPipelineStageFlags.class) int dstStageMask
        ) {
            this.barriers = barriers;
            this.srcStageMask = srcStageMask;
            this.dstStageMask = dstStageMask;
        }

        @Override
        public void toCommandBuffer(VulkanRenderEngine engine, VkCommandBuffer commandBuffer) {
            engine.cx.dCmd.vkCmdPipelineBarrier(
                    commandBuffer,
                    srcStageMask,
                    dstStageMask,
                    0,
                    0, null,
                    0, null,
                    barriers.length, barriers[0]
            );
        }
    }

    private static final class BeginRendering implements CompiledPipelineComponent {
        public final VkRenderingInfo renderingInfo;
        public final VkViewport viewport;
        public final VkRect2D scissor;

        public final int attachmentWidth;
        public final int attachmentHeight;

        private BeginRendering(
                VkRenderingInfo renderingInfo,
                VkViewport viewport,
                VkRect2D scissor,
                int attachmentWidth,
                int attachmentHeight
        ) {
            this.renderingInfo = renderingInfo;
            this.viewport = viewport;
            this.scissor = scissor;
            this.attachmentWidth = attachmentWidth;
            this.attachmentHeight = attachmentHeight;
        }

        @Override
        public void toCommandBuffer(VulkanRenderEngine engine, VkCommandBuffer commandBuffer) {
            if (attachmentWidth == -1 || attachmentHeight == -1) {
                int w = attachmentWidth == -1 ? engine.swapchain.swapExtent.width() : attachmentWidth;
                int h = attachmentHeight == -1 ? engine.swapchain.swapExtent.height() : attachmentHeight;

                viewport.width(w);
                viewport.height(h);
                VkExtent2D extent = scissor.extent();
                extent.width(w);
                extent.height(h);
            }

            engine.cx.dCmd.vkCmdBeginRendering(commandBuffer, renderingInfo);
            engine.cx.dCmd.vkCmdSetViewport(commandBuffer, 0, 1, viewport);
            engine.cx.dCmd.vkCmdSetScissor(commandBuffer, 0, 1, scissor);
        }
    }

    private static final class BindPipeline implements CompiledPipelineComponent {
        private final VkPipeline pipeline;

        private BindPipeline(VkPipeline pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public void toCommandBuffer(VulkanRenderEngine engine, VkCommandBuffer commandBuffer) {
            engine.cx.dCmd.vkCmdBindPipeline(
                    commandBuffer,
                    VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pipeline
            );
        }
    }

    private static final class EndRendering implements CompiledPipelineComponent {
        @Override
        public void toCommandBuffer(VulkanRenderEngine engine, VkCommandBuffer commandBuffer) {
            engine.cx.dCmd.vkCmdEndRendering(commandBuffer);
        }
    }

    private static final class BindDescriptorSet implements CompiledPipelineComponent {
        private final VkPipelineLayout pipelineLayout;
        private final VkDescriptorSet.Buffer pDescriptorSet;
        private final int targetSet;

        private BindDescriptorSet(
                VkPipelineLayout pipelineLayout,
                VkDescriptorSet.Buffer pDescriptorSet,
                int targetSet
        ) {
            this.pipelineLayout = pipelineLayout;
            this.pDescriptorSet = pDescriptorSet;
            this.targetSet = targetSet;
        }

        @Override
        public void toCommandBuffer(VulkanRenderEngine engine, VkCommandBuffer commandBuffer) {
            engine.cx.dCmd.vkCmdBindDescriptorSets(
                    commandBuffer,
                    VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
                    pipelineLayout,
                    targetSet,
                    1, pDescriptorSet,
                    0, null
            );
        }
    }
}
