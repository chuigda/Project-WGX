package chr.wgx.render.vk.compiled;

import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.data.VulkanSwapchainAttachment;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkAccessFlags;
import club.doki7.vulkan.bitmask.VkImageAspectFlags;
import club.doki7.vulkan.bitmask.VkPipelineStageFlags;
import club.doki7.vulkan.datatype.VkImageMemoryBarrier;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.handle.VkCommandBuffer;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public final class ImageBarrierOp implements CompiledRenderPassOp {
    public final List<VulkanAttachment> attachments;
    public final VkImageMemoryBarrier.Ptr barriers;

    public ImageBarrierOp(
            VulkanRenderEngineContext cx,
            List<VulkanAttachment> attachments,
            @EnumType(VkImageLayout.class) List<Integer> oldLayouts,
            @EnumType(VkImageLayout.class) List<Integer> newLayouts
    ) {
        assert attachments.size() == oldLayouts.size()
               && attachments.size() == newLayouts.size();

        this.attachments = attachments;
        barriers = VkImageMemoryBarrier.allocate(cx.prefabArena, attachments.size());
        for (int i = 0; i < attachments.size(); i++) {
            VulkanAttachment attachment = attachments.get(i);
            VkImageMemoryBarrier barrier = barriers.at(i);

            @EnumType(VkImageAspectFlags.class) int aspectMask =
                    attachment.createInfo.pixelFormat == PixelFormat.DEPTH_BUFFER_OPTIMAL
                            ? VkImageAspectFlags.DEPTH
                            : VkImageAspectFlags.COLOR;

            @EnumType(VkImageLayout.class) int oldLayout = oldLayouts.get(i);
            @EnumType(VkImageLayout.class) int newLayout = newLayouts.get(i);
            Pair<Integer, Integer> accessMasks = chooseAccessMasks(oldLayout, newLayout);

            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcAccessMask(accessMasks.first());
            barrier.dstAccessMask(accessMasks.second());
            barrier.srcQueueFamilyIndex(VkConstants.QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VkConstants.QUEUE_FAMILY_IGNORED);
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
            VkImageMemoryBarrier barrier = barriers.at(i);

            switch (attachment) {
                case VulkanImageAttachment imageAttachment -> barrier.image(
                        imageAttachment.image.value.image
                );
                case VulkanSwapchainAttachment swapchainAttachment -> barrier.image(
                        swapchainAttachment.swapchainImage.image
                );
            }
        }

        cx.dCmd.cmdPipelineBarrier(
                cmdBuf,
                VkPipelineStageFlags.COLOR_ATTACHMENT_OUTPUT,
                VkPipelineStageFlags.COLOR_ATTACHMENT_OUTPUT
                | VkPipelineStageFlags.FRAGMENT_SHADER
                | VkPipelineStageFlags.TRANSFER,
                0,
                0, null,
                0, null,
                (int) barriers.size(), barriers
        );
    }

    private static @EnumType(VkAccessFlags.class) Pair<Integer, Integer> chooseAccessMasks(
            @EnumType(VkImageLayout.class) int oldLayout,
            @EnumType(VkImageLayout.class) int newLayout
    ) {
        if (oldLayout == VkImageLayout.UNDEFINED) {
            if (newLayout == VkImageLayout.COLOR_ATTACHMENT_OPTIMAL) {
                return new Pair<>(0, VkAccessFlags.COLOR_ATTACHMENT_WRITE);
            } else if (newLayout == VkImageLayout.SHADER_READ_ONLY_OPTIMAL) {
                return new Pair<>(0, VkAccessFlags.SHADER_READ);
            } else if (newLayout == VkImageLayout.PRESENT_SRC_KHR) {
                return new Pair<>(0, VkAccessFlags.MEMORY_READ);
            }
        }
        else if (oldLayout == VkImageLayout.COLOR_ATTACHMENT_OPTIMAL
                 && newLayout == VkImageLayout.SHADER_READ_ONLY_OPTIMAL) {
            return new Pair<>(VkAccessFlags.COLOR_ATTACHMENT_WRITE, VkAccessFlags.SHADER_WRITE);
        }
        else if (oldLayout == VkImageLayout.COLOR_ATTACHMENT_OPTIMAL
                 && newLayout == VkImageLayout.PRESENT_SRC_KHR) {
            return new Pair<>(VkAccessFlags.COLOR_ATTACHMENT_WRITE, VkAccessFlags.MEMORY_READ);
        }
        else if (oldLayout == VkImageLayout.COLOR_ATTACHMENT_OPTIMAL
                 && newLayout == VkImageLayout.COLOR_ATTACHMENT_OPTIMAL) {
            return new Pair<>(VkAccessFlags.COLOR_ATTACHMENT_WRITE, VkAccessFlags.COLOR_ATTACHMENT_WRITE);
        }

        throw new UnsupportedOperationException("不支持的布局转换: 从 " + VkImageLayout.explain(oldLayout) + " 到 " + VkImageLayout.explain(newLayout));
    }
}
