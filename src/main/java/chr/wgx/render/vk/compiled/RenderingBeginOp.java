package chr.wgx.render.vk.compiled;

import chr.wgx.render.common.Color;
import chr.wgx.render.vk.Resource;
import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.data.VulkanSwapchainAttachment;
import tech.icey.vk4j.bitmask.VkResolveModeFlags;
import tech.icey.vk4j.datatype.VkRect2D;
import tech.icey.vk4j.datatype.VkRenderingAttachmentInfo;
import tech.icey.vk4j.datatype.VkRenderingInfo;
import tech.icey.vk4j.datatype.VkViewport;
import tech.icey.vk4j.enumtype.VkAttachmentLoadOp;
import tech.icey.vk4j.enumtype.VkAttachmentStoreOp;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;

public final class RenderingBeginOp implements CompiledRenderPassOp {
    private final List<VulkanAttachment> colorAttachments;
    private final List<Color> clearColors;
    private final Option<VulkanImageAttachment> depthAttachment;

    private final VkRenderingInfo renderingInfo;
    private final VkRenderingAttachmentInfo[] colorAttachmentInfos;
    private final Option<VkRenderingAttachmentInfo> depthAttachmentInfo;

    public RenderingBeginOp(
            VulkanRenderEngineContext cx,
            List<VulkanAttachment> colorAttachments,
            List<Color> clearColors,
            Option<VulkanImageAttachment> depthAttachment,

            List<Boolean> colorAttachmentInitialized,
            List<Boolean> colorAttachmentUsedInFuture,
            boolean depthAttachmentInitialized,
            boolean depthAttachmentUsedInFuture
    ) {
        this.colorAttachments = colorAttachments;
        this.clearColors = clearColors;
        this.depthAttachment = depthAttachment;

        renderingInfo = VkRenderingInfo.allocate(cx.prefabArena);
        colorAttachmentInfos = VkRenderingAttachmentInfo.allocate(cx.prefabArena, colorAttachments.size());
        for (int i = 0; i < colorAttachments.size(); i++) {
            VkRenderingAttachmentInfo colorAttachmentInfo = colorAttachmentInfos[i];
            VulkanAttachment colorAttachment = colorAttachments.get(i);

            colorAttachmentInfo.imageLayout(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            if (colorAttachmentInitialized.get(i)) {
                colorAttachmentInfo.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_LOAD);
            } else {
                colorAttachmentInfo.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR);
                clearColors.get(i).writeTo(colorAttachmentInfo.clearValue().color());
            }

            if (colorAttachment instanceof VulkanSwapchainAttachment swapchainAttachment
                && swapchainAttachment.msaaColorImage.isSome()) {
                colorAttachmentInfo.storeOp(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE);
                colorAttachmentInfo.resolveMode(VkResolveModeFlags.VK_RESOLVE_MODE_AVERAGE_BIT);
                colorAttachmentInfo.resolveImageLayout(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            } else {
                colorAttachmentInfo.storeOp(
                        colorAttachmentUsedInFuture.get(i)
                                ? VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE
                                : VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE
                );
            }
        }

        depthAttachmentInfo = depthAttachment.map(_ -> {
            VkRenderingAttachmentInfo info = VkRenderingAttachmentInfo.allocate(cx.prefabArena);
            info.imageLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            if (depthAttachmentInitialized) {
                info.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_LOAD);
            } else {
                info.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR);
                info.clearValue().depthStencil().depth(1.0f);
            }
            if (depthAttachmentUsedInFuture) {
                info.storeOp(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE);
            } else {
                info.storeOp(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE);
            }
            return info;
        });

        renderingInfo.layerCount(1);
        renderingInfo.colorAttachmentCount(colorAttachmentInfos.length);
        renderingInfo.pColorAttachments(colorAttachmentInfos[0]);
        if (depthAttachmentInfo instanceof Option.Some<VkRenderingAttachmentInfo> some) {
            renderingInfo.pDepthAttachment(some.value);
        }
    }

    @Override
    public void recordToCommandBuffer(
            VulkanRenderEngineContext cx,
            Swapchain swapchain,
            VkCommandBuffer cmdBuf,
            int frameIndex
    ) {
        renderingInfo.renderArea().extent(swapchain.swapExtent);

        for (int i = 0; i < colorAttachments.size(); i++) {
            VulkanAttachment colorAttachment = colorAttachments.get(i);
            VkRenderingAttachmentInfo colorAttachmentInfo = colorAttachmentInfos[i];

            switch (colorAttachment) {
                case VulkanSwapchainAttachment swapchainAttachment -> {
                    if (swapchainAttachment.msaaColorImage instanceof Option.Some<Resource.Image> some) {
                        colorAttachmentInfo.imageView(some.value.imageView);
                        colorAttachmentInfo.resolveImageView(swapchainAttachment.swapchainImage.imageView);
                    } else {
                        colorAttachmentInfo.imageView(swapchainAttachment.swapchainImage.imageView);
                    }
                }
                case VulkanImageAttachment imageAttachment -> colorAttachmentInfo.imageView(
                        imageAttachment.image.value.imageView
                );
            }
        }

        if (depthAttachmentInfo instanceof Option.Some<VkRenderingAttachmentInfo> some) {
            VulkanImageAttachment attachment = depthAttachment.get();
            some.value.imageView(attachment.image.value.imageView);
        }

        cx.dCmd.vkCmdBeginRendering(cmdBuf, renderingInfo);

        int renderAreaWidth = colorAttachments.getFirst().createInfo.width;
        int renderAreaHeight = colorAttachments.getFirst().createInfo.height;
        if (renderAreaWidth == -1) {
            renderAreaWidth = swapchain.swapExtent.width();
            renderAreaHeight = swapchain.swapExtent.height();
        }

        try (Arena arena = Arena.ofConfined()) {
            VkViewport viewport = VkViewport.allocate(arena);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(renderAreaWidth);
            viewport.height(renderAreaHeight);
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);
            cx.dCmd.vkCmdSetViewport(cmdBuf, 0, 1, viewport);

            VkRect2D scissor = VkRect2D.allocate(arena);
            scissor.extent().width(renderAreaWidth);
            scissor.extent().height(renderAreaHeight);
            cx.dCmd.vkCmdSetScissor(cmdBuf, 0, 1, scissor);
        }
    }
}
