package chr.wgx.render.vk.compiled;

import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.data.VulkanAttachment;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.VkImageMemoryBarrier;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.vk4j.handle.VkCommandBuffer;

import java.util.List;

public final class ImageBarrierOp implements CompiledRenderPassOp {
    public final VkImageMemoryBarrier[] barriers;

    public ImageBarrierOp(
            VulkanRenderEngineContext cx,
            List<VulkanAttachment> attachments,
            @enumtype(VkImageLayout.class) List<Integer> oldLayout,
            @enumtype(VkImageLayout.class) List<Integer> newLayout
    ) {
        assert attachments.size() == oldLayout.size()
               && attachments.size() == newLayout.size();

        barriers = VkImageMemoryBarrier.allocate(cx.prefabArena, attachments.size());
        for (int i = 0; i < attachments.size(); i++) {
            VulkanAttachment attachment = attachments.get(i);
            VkImageMemoryBarrier barrier = barriers[i];

            @enumtype(VkImageAspectFlags.class) int aspectMask =
                    attachment.createInfo.pixelFormat == PixelFormat.DEPTH_BUFFER_OPTIMAL
                            ? VkImageAspectFlags.VK_IMAGE_ASPECT_DEPTH_BIT
                            : VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT;

            barrier.oldLayout(oldLayout.get(i));
            barrier.newLayout(newLayout.get(i));
            barrier.srcQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
            barrier.image(attachment.image.value.image);
            barrier.subresourceRange().aspectMask(aspectMask);
            barrier.subresourceRange().baseMipLevel(0);
            barrier.subresourceRange().levelCount(1);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);
        }
    }

    @Override
    public void recordToCommandBuffer(VulkanRenderEngineContext cx, VkCommandBuffer cmdbuf, int frameIndex) {
        cx.dCmd.vkCmdPipelineBarrier(
                cmdbuf,
                VkPipelineStageFlags.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                VkPipelineStageFlags.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                0,
                0, null,
                0, null,
                barriers.length, barriers[0]
        );
    }
}
