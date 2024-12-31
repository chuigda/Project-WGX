package chr.wgx.render.vk;

import chr.wgx.render.common.ClearBehavior;
import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.data.Attachment;
import chr.wgx.render.info.RenderPassAttachmentInfo;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.vk.compiled.*;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.task.VulkanRenderPass;
import chr.wgx.render.vk.task.VulkanRenderPipelineBind;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.xjbutil.container.Option;

import java.util.*;

public final class ASPECT_RenderPassCompilation {
    private final VulkanRenderEngine engine;

    public ASPECT_RenderPassCompilation(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public void recompileRenderPasses() {
        List<CompiledRenderPassOp> compiled = new ArrayList<>();
        HashMap<VulkanAttachment, Integer> currentLayouts = new HashMap<>();
        HashSet<VulkanAttachment> attachmentInitialized = new HashSet<>();
        HashMap<Attachment, Integer> attachmentUsageCounts = getAttachmentFutureUsageCounts();

        for (VulkanRenderPass renderPass : engine.renderPasses) {
            List<VulkanAttachment> transformedAttachments = new ArrayList<>();
            @enumtype(VkImageLayout.class) List<Integer> oldLayout = new ArrayList<>();
            @enumtype(VkImageLayout.class) List<Integer> newLayout = new ArrayList<>();

            // 将所有输入附件的布局转换为可供组合图像采样器使用的布局
            for (VulkanAttachment inputAttachment : renderPass.inputAttachments) {
                if (inputAttachment.createInfo.pixelFormat == PixelFormat.DEPTH_BUFFER_OPTIMAL) {
                    // TODO 在未来的版本中考虑允许对深度附件采样
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
            for (VulkanAttachment colorAttachment : renderPass.colorAttachments) {
                @enumtype(VkImageLayout.class) int currentLayout = currentLayouts.getOrDefault(
                        colorAttachment,
                        VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED
                );
                if (currentLayout != VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
                    transformedAttachments.add(colorAttachment);
                    oldLayout.add(currentLayout);
                    newLayout.add(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
                }

                currentLayouts.put(colorAttachment, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            }

            if (!transformedAttachments.isEmpty()) {
                compiled.add(new ImageBarrierOp(engine.cx, transformedAttachments, oldLayout, newLayout));
            }

            updateAttachmentFutureUsageCounts(attachmentUsageCounts, renderPass);

            List<Boolean> colorAttachmentInitialized = renderPass.colorAttachments.stream()
                    .map(attachmentInitialized::contains)
                    .toList();
            // 分析附件被写入之后，后续的使用情况
            List<Boolean> colorAttachmentUsedInFuture = renderPass.colorAttachments.stream()
                    .map(att -> attachmentUsageCounts.getOrDefault(att, 0) > 0)
                    .toList();
            attachmentInitialized.addAll(renderPass.colorAttachments);
            boolean depthAttachmentInitialized;
            if (renderPass.depthAttachment instanceof Option.Some<VulkanImageAttachment> some) {
                depthAttachmentInitialized = attachmentInitialized.contains(some.value);
            } else {
                depthAttachmentInitialized = false;
            }
            boolean depthAttachmentUsedInFuture = true; // TODO: 分析深度附件被写入之后，后续的使用情况

            compiled.add(new RenderingBeginOp(
                    engine.cx,
                    renderPass,

                    colorAttachmentInitialized,
                    colorAttachmentUsedInFuture,
                    depthAttachmentInitialized,
                    depthAttachmentUsedInFuture
            ));

            for (VulkanRenderPipelineBind bindPoint : renderPass.bindList) {
                compiled.add(new RenderOp(bindPoint));
            }

            compiled.add(new RenderingEndOp());
        }

        // 将交换链颜色附件的布局转换为可供呈现使用的布局
        compiled.add(new ImageBarrierOp(
                engine.cx,
                List.of(engine.swapchainColorAttachment),
                List.of(currentLayouts.getOrDefault(
                        engine.swapchainColorAttachment,
                        VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED
                )),
                List.of(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
        ));

        engine.compiledRenderPassOps = compiled;
    }

    private HashMap<Attachment, Integer> getAttachmentFutureUsageCounts() {
        HashMap<Attachment, Integer> totalFutureUsageCounts = new HashMap<>();
        for (VulkanRenderPass renderPass : engine.renderPasses) {
            // 如果是 input，则被用到
            for (VulkanAttachment attachment : renderPass.inputAttachments) {
                totalFutureUsageCounts.compute(attachment, (_, value) -> value == null ? 1 : value + 1);
            }
            // 如果 output 清除方式是 CLEAR_NEVER 或 CLEAR_ONCE，则被用到
            for (RenderPassAttachmentInfo attachmentInfo : renderPass.info.colorAttachmentInfos) {
                if (attachmentInfo.clearBehavior == ClearBehavior.CLEAR_NEVER
                        || attachmentInfo.clearBehavior == ClearBehavior.CLEAR_ONCE) {
                    totalFutureUsageCounts.compute(
                            attachmentInfo.attachment,
                            (_, value) -> value == null ? 1 : value + 1);
                }
            }
            if (renderPass.info.depthAttachmentInfo.isSome() && (
                    renderPass.info.depthAttachmentInfo.get().clearBehavior == ClearBehavior.CLEAR_NEVER
                    || renderPass.info.depthAttachmentInfo.get().clearBehavior == ClearBehavior.CLEAR_ONCE)
            ) {
                totalFutureUsageCounts.compute(
                        renderPass.info.depthAttachmentInfo.get().attachment,
                        (_, value) -> value == null ? 1 : value + 1);
            }
        }
        return totalFutureUsageCounts;
    }

    private void updateAttachmentFutureUsageCounts(HashMap<Attachment, Integer> futureUsageCounts, VulkanRenderPass renderPass) {
        // 如果是 input，则被用到，计数 -1
        for (VulkanAttachment attachment : renderPass.inputAttachments) {
            // map 中如果没有这个元素，说明值发生变化，会触发下一次 recompile。这次将会是未定义行为。直接返回 null。
            futureUsageCounts.compute(attachment, (_, cnt) -> cnt == null ? null : cnt - 1);
        }
        // 如果 output 清除方式是 CLEAR_NEVER 或 CLEAR_ONCE，则被用到，计数 -1
        for (RenderPassAttachmentInfo attachmentInfo : renderPass.info.colorAttachmentInfos) {
            if (attachmentInfo.clearBehavior == ClearBehavior.CLEAR_NEVER
                    || attachmentInfo.clearBehavior == ClearBehavior.CLEAR_ONCE) {
                futureUsageCounts.compute(
                        attachmentInfo.attachment,
                        (_, cnt) -> cnt == null ? null : cnt - 1);
            }
        }
        if (renderPass.info.depthAttachmentInfo.isSome() && (
                renderPass.info.depthAttachmentInfo.get().clearBehavior == ClearBehavior.CLEAR_NEVER
                        || renderPass.info.depthAttachmentInfo.get().clearBehavior == ClearBehavior.CLEAR_ONCE)
        ) {
            futureUsageCounts.compute(
                    renderPass.info.depthAttachmentInfo.get().attachment,
                    (_, cnt) -> cnt == null ? null : cnt - 1);
        }
    }
}
