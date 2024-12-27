package chr.wgx.render.vk.compiled;

import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.data.VulkanSwapchainAttachment;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkAccessFlags;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.VkImageMemoryBarrier;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public final class ImageBarrierOp implements CompiledRenderPassOp {
    public final List<VulkanAttachment> attachments;
    public final VkImageMemoryBarrier[] barriers;

    public ImageBarrierOp(
            VulkanRenderEngineContext cx,
            List<VulkanAttachment> attachments,
            @enumtype(VkImageLayout.class) List<Integer> oldLayouts,
            @enumtype(VkImageLayout.class) List<Integer> newLayouts
    ) {
        assert attachments.size() == oldLayouts.size()
               && attachments.size() == newLayouts.size();

        this.attachments = attachments;
        barriers = VkImageMemoryBarrier.allocate(cx.prefabArena, attachments.size());
        for (int i = 0; i < attachments.size(); i++) {
            VulkanAttachment attachment = attachments.get(i);
            VkImageMemoryBarrier barrier = barriers[i];

            @enumtype(VkImageAspectFlags.class) int aspectMask =
                    attachment.createInfo.pixelFormat == PixelFormat.DEPTH_BUFFER_OPTIMAL
                            ? VkImageAspectFlags.VK_IMAGE_ASPECT_DEPTH_BIT
                            : VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT;

            @enumtype(VkImageLayout.class) int oldLayout = oldLayouts.get(i);
            @enumtype(VkImageLayout.class) int newLayout = newLayouts.get(i);
            Pair<Integer, Integer> accessMasks = chooseAccessMasks(oldLayout, newLayout);

            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcAccessMask(accessMasks.first());
            barrier.dstAccessMask(accessMasks.second());
            barrier.srcQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(Constants.VK_QUEUE_FAMILY_IGNORED);
            barrier.subresourceRange().aspectMask(aspectMask);
            barrier.subresourceRange().baseMipLevel(0);
            barrier.subresourceRange().levelCount(1);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);
        }
    }

    @Override
    public void recordToCommandBuffer(
            VulkanRenderEngineContext cx,
            Swapchain swapchain,
            VkCommandBuffer cmdBuf,
            int frameIndex
    ) {
        for (int i = 0; i < attachments.size(); i++) {
            VulkanAttachment attachment = attachments.get(i);
            VkImageMemoryBarrier barrier = barriers[i];

            switch (attachment) {
                case VulkanImageAttachment imageAttachment -> barrier.image(
                        imageAttachment.image.value.image
                );
                case VulkanSwapchainAttachment swapchainAttachment -> barrier.image(
                        swapchainAttachment.swapchainImage.image
                );
            }
        }

        cx.dCmd.vkCmdPipelineBarrier(
                cmdBuf,
                VkPipelineStageFlags.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                VkPipelineStageFlags.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT
                | VkPipelineStageFlags.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT
                | VkPipelineStageFlags.VK_PIPELINE_STAGE_TRANSFER_BIT,
                0,
                0, null,
                0, null,
                barriers.length, barriers[0]
        );
    }

    private static @enumtype(VkAccessFlags.class) Pair<Integer, Integer> chooseAccessMasks(
            @enumtype(VkImageLayout.class) int oldLayout,
            @enumtype(VkImageLayout.class) int newLayout
    ) {
        if (oldLayout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) {
            if (newLayout == VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
                return new Pair<>(0, VkAccessFlags.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
            } else if (newLayout == VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                return new Pair<>(0, VkAccessFlags.VK_ACCESS_SHADER_READ_BIT);
            } else if (newLayout == VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR) {
                return new Pair<>(0, VkAccessFlags.VK_ACCESS_MEMORY_READ_BIT);
            }
        }
        else if (oldLayout == VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                 && newLayout == VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
            return new Pair<>(VkAccessFlags.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VkAccessFlags.VK_ACCESS_SHADER_READ_BIT);
        }
        else if (oldLayout == VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                 && newLayout == VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR) {
            return new Pair<>(VkAccessFlags.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT, VkAccessFlags.VK_ACCESS_MEMORY_READ_BIT);
        }

        throw new UnsupportedOperationException("不支持的布局转换: 从 " + VkImageLayout.explain(oldLayout) + " 到 " + VkImageLayout.explain(newLayout));
    }
}
