package chr.wgx.render.vk.compiled;

import chr.wgx.render.info.RenderPassAttachmentInfo;
import chr.wgx.render.vk.Swapchain;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.data.VulkanSwapchainAttachment;
import chr.wgx.render.vk.task.VulkanRenderPass;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.VkAttachmentLoadOp;
import club.doki7.vulkan.enumtype.VkAttachmentStoreOp;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.handle.VkCommandBuffer;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public final class RenderingBeginOp implements CompiledRenderPassOp {
    private final VulkanRenderPass renderPass;

    private final VkRenderingInfo renderingInfo;
    private final VkRenderingAttachmentInfo.Ptr colorAttachmentInfos;
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
            VkRenderingAttachmentInfo colorAttachmentInfo = colorAttachmentInfos.at(i);
            RenderPassAttachmentInfo renderPassAttachmentInfo = renderPass.info.colorAttachmentInfos.get(i);

            colorAttachmentInfo.imageLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

            if (colorAttachmentNeedClear.get(i)) {
                colorAttachmentInfo.loadOp(VkAttachmentLoadOp.CLEAR);
                renderPassAttachmentInfo.clearColor.writeTo(colorAttachmentInfo.clearValue().color());
            } else {
                colorAttachmentInfo.loadOp(VkAttachmentLoadOp.LOAD);
            }

            colorAttachmentInfo.storeOp(
                    colorAttachmentUsedInFuture.get(i)
                            ? VkAttachmentStoreOp.STORE
                            : VkAttachmentStoreOp.DONT_CARE
            );
        }

        depthAttachmentInfo = renderPass.info.depthAttachmentInfo.map(renderPassAttachmentInfo -> {
            VkRenderingAttachmentInfo info = VkRenderingAttachmentInfo.allocate(cx.prefabArena);

            info.imageLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            if (depthAttachmentNeedClear) {
                info.loadOp(VkAttachmentLoadOp.CLEAR);
                info.clearValue().depthStencil().depth(1.0f);
            } else {
                info.loadOp(VkAttachmentLoadOp.LOAD);
            }

            info.storeOp(
                    depthAttachmentUsedInFuture
                            ? VkAttachmentStoreOp.STORE
                            : VkAttachmentStoreOp.DONT_CARE
            );
            return info;
        });

        renderingInfo.layerCount(1);
        renderingInfo.colorAttachmentCount((int) colorAttachmentInfos.size());
        renderingInfo.pColorAttachments(colorAttachmentInfos);
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

        for (int i = 0; i < colorAttachmentInfos.size(); i++) {
            VulkanAttachment colorAttachment = renderPass.colorAttachments.get(i);
            VkRenderingAttachmentInfo colorAttachmentInfo = colorAttachmentInfos.at(i);

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

        cx.dCmd.cmdBeginRendering(cmdBuf, renderingInfo);

        viewport.width(renderAreaWidth);
        viewport.height(renderAreaHeight);
        cx.dCmd.cmdSetViewport(cmdBuf, 0, 1, viewport);

        scissorExtent.width(renderAreaWidth);
        scissorExtent.height(renderAreaHeight);
        cx.dCmd.cmdSetScissor(cmdBuf, 0, 1, scissor);
    }
}
