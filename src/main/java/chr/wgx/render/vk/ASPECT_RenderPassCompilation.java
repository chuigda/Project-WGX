package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.vk.compiled.CompiledRenderPassOp;
import chr.wgx.render.vk.compiled.ImageBarrierOp;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.task.VulkanRenderPass;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkImageLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public final class ASPECT_RenderPassCompilation {
    private final VulkanRenderEngine engine;

    public ASPECT_RenderPassCompilation(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public void recompileRenderPasses() throws RenderException {
        List<CompiledRenderPassOp> compiled = new ArrayList<>();
        HashMap<VulkanAttachment, Integer> currentLayouts = new HashMap<>();
        HashSet<VulkanAttachment> attachmentInitialized = new HashSet<>();

        currentLayouts.put(
                engine.swapchainColorAttachment,
                VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
        );
        currentLayouts.put(
                engine.swapchainDepthAttachment,
                VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL
        );

        for (VulkanRenderPass renderPass : engine.renderPasses) {
            List<VulkanAttachment> transformedAttachments = new ArrayList<>();
            @enumtype(VkImageLayout.class) List<Integer> oldLayout = new ArrayList<>();
            @enumtype(VkImageLayout.class) List<Integer> newLayout = new ArrayList<>();

            // 将所有输入附件的布局转换为可供组合图像采样器使用的布局
            for (VulkanAttachment inputAttachment : renderPass.inputAttachments) {
                if (inputAttachment.createInfo.pixelFormat == PixelFormat.DEPTH_BUFFER_OPTIMAL) {
                    // TODO 在未来的版本中允许对深度附件采样
                    continue;
                }

                @enumtype(VkImageLayout.class) int currentLayout = currentLayouts.getOrDefault(
                        inputAttachment,
                        VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED
                );
                if (currentLayout != VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                    transformedAttachments.add(inputAttachment);
                    oldLayout.add(currentLayout);
                    newLayout.add(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                }
            }
            // 将所有输出附件的布局转换为可供颜色附件使用的布局
            for (VulkanAttachment outputAttachment : renderPass.outputAttachments) {
                if (outputAttachment.createInfo.pixelFormat == PixelFormat.DEPTH_BUFFER_OPTIMAL) {
                    continue;
                }

                @enumtype(VkImageLayout.class) int currentLayout = currentLayouts.getOrDefault(
                        outputAttachment,
                        VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED
                );
                if (currentLayout != VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
                    transformedAttachments.add(outputAttachment);
                    oldLayout.add(currentLayout);
                    newLayout.add(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
                }
            }

            if (!transformedAttachments.isEmpty()) {
                compiled.add(new ImageBarrierOp(engine.cx, transformedAttachments, oldLayout, newLayout));
            }
        }
    }
}
