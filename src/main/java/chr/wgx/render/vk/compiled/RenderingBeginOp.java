package chr.wgx.render.vk.compiled;

import chr.wgx.render.info.RenderPassAttachmentInfo;
import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.data.VulkanSwapchainAttachment;
import chr.wgx.render.vk.task.VulkanRenderPass;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.VkAttachmentLoadOp;
import tech.icey.vk4j.enumtype.VkAttachmentStoreOp;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public final class RenderingBeginOp implements CompiledRenderPassOp {
    private final VulkanRenderPass renderPass;

    private final VkRenderingInfo renderingInfo;
    private final VkRenderingAttachmentInfo[] colorAttachmentInfos;
    private final Option<VkRenderingAttachmentInfo> depthAttachmentInfo;

    private final VkExtent2D renderAreaExtent;
    private final VkViewport viewport;
    private final VkRect2D scissor;
    private final VkExtent2D scissorExtent;

    public RenderingBeginOp(
            VulkanRenderEngineContext cx,
            VulkanRenderPass renderPass,

            List<Boolean> colorAttachmentNeedClear,
            List<Boolean> colorAttachmentUsedInFuture,
            boolean depthAttachmentNeedClear,
            boolean depthAttachmentUsedInFuture
    ) {
        this.renderPass = renderPass;

        int renderAreaWidth = renderPass.renderAreaWidth;
        int renderAreaHeight = renderPass.renderAreaHeight;

        renderingInfo = VkRenderingInfo.allocate(cx.prefabArena);
        if (renderAreaWidth != -1) {
            renderingInfo.renderArea().extent().width(renderAreaWidth);
            renderingInfo.renderArea().extent().height(renderAreaHeight);
        }

        colorAttachmentInfos = VkRenderingAttachmentInfo.allocate(
                cx.prefabArena,
                renderPass.info.colorAttachmentInfos.size()
        );
        for (int i = 0; i < renderPass.info.colorAttachmentInfos.size(); i++) {
            VkRenderingAttachmentInfo colorAttachmentInfo = colorAttachmentInfos[i];
            RenderPassAttachmentInfo renderPassAttachmentInfo = renderPass.info.colorAttachmentInfos.get(i);

            colorAttachmentInfo.imageLayout(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            if (colorAttachmentNeedClear.get(i)) {
                colorAttachmentInfo.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR);
                renderPassAttachmentInfo.clearColor.writeTo(colorAttachmentInfo.clearValue().color());
            } else {
                colorAttachmentInfo.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_LOAD);
            }

            colorAttachmentInfo.storeOp(
                    colorAttachmentUsedInFuture.get(i)
                            ? VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE
                            : VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE
            );
        }

        depthAttachmentInfo = renderPass.info.depthAttachmentInfo.map(renderPassAttachmentInfo -> {
            VkRenderingAttachmentInfo info = VkRenderingAttachmentInfo.allocate(cx.prefabArena);

            info.imageLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            if (depthAttachmentNeedClear) {
                info.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR);
                info.clearValue().depthStencil().depth(1.0f);
            } else {
                info.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_LOAD);
            }

            info.storeOp(
                    depthAttachmentUsedInFuture
                            ? VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE
                            : VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE
            );
            return info;
        });

        renderingInfo.layerCount(1);
        renderingInfo.colorAttachmentCount(colorAttachmentInfos.length);
        renderingInfo.pColorAttachments(colorAttachmentInfos[0]);
        if (depthAttachmentInfo instanceof Option.Some<VkRenderingAttachmentInfo> some) {
            renderingInfo.pDepthAttachment(some.value);
        }

        renderAreaExtent = renderingInfo.renderArea().extent();

        this.viewport = VkViewport.allocate(cx.prefabArena);
        viewport.minDepth(0.0f);
        viewport.maxDepth(1.0f);
        this.scissor = VkRect2D.allocate(cx.prefabArena);
        this.scissorExtent = scissor.extent();
    }

    @Override
    public void recordToCommandBuffer(
            VulkanRenderEngineContext cx,
            Swapchain swapchain,
            VkCommandBuffer cmdBuf,
            int frameIndex
    ) {
        int renderAreaWidth = renderPass.renderAreaWidth;
        int renderAreaHeight = renderPass.renderAreaHeight;
        if (renderAreaWidth == -1) {
            renderAreaWidth = swapchain.swapExtent.width();
            renderAreaHeight = swapchain.swapExtent.height();
        }

        renderAreaExtent.width(renderAreaWidth);
        renderAreaExtent.height(renderAreaHeight);

        for (int i = 0; i < colorAttachmentInfos.length; i++) {
            VulkanAttachment colorAttachment = renderPass.colorAttachments.get(i);
            VkRenderingAttachmentInfo colorAttachmentInfo = colorAttachmentInfos[i];

            switch (colorAttachment) {
                case VulkanSwapchainAttachment swapchainAttachment -> colorAttachmentInfo.imageView(
                        swapchainAttachment.swapchainImage.imageView
                );
                case VulkanImageAttachment imageAttachment -> colorAttachmentInfo.imageView(
                        imageAttachment.image.value.imageView
                );
            }
        }

        if (depthAttachmentInfo instanceof Option.Some<VkRenderingAttachmentInfo> some) {
            VulkanImageAttachment attachment = renderPass.depthAttachment.get();
            some.value.imageView(attachment.image.value.imageView);
        }

        cx.dCmd.vkCmdBeginRendering(cmdBuf, renderingInfo);

        viewport.width(renderAreaWidth);
        viewport.height(renderAreaHeight);
        cx.dCmd.vkCmdSetViewport(cmdBuf, 0, 1, viewport);

        scissorExtent.width(renderAreaWidth);
        scissorExtent.height(renderAreaHeight);
        cx.dCmd.vkCmdSetScissor(cmdBuf, 0, 1, scissor);
    }
}
