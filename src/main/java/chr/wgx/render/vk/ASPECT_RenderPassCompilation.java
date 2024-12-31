package chr.wgx.render.vk;

import chr.wgx.render.common.ClearBehavior;
import chr.wgx.render.info.RenderPassAttachmentInfo;
import chr.wgx.render.vk.compiled.*;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.task.VulkanRenderPass;
import chr.wgx.render.vk.task.VulkanRenderPipelineBind;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.xjbutil.container.Option;

import java.util.*;
import java.util.logging.Logger;

public final class ASPECT_RenderPassCompilation {
    private final VulkanRenderEngine engine;

    public ASPECT_RenderPassCompilation(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public void recompileRenderPasses() {
        List<CompiledRenderPassOp> compiled = new ArrayList<>();
        HashMap<VulkanAttachment, Integer> currentLayouts = new HashMap<>();
        HashSet<VulkanAttachment> attachmentCleared = new HashSet<>();

        List<VulkanRenderPass> renderPassList = engine.renderPasses.stream().toList();
        for (int renderPassIndex = 0; renderPassIndex < renderPassList.size(); renderPassIndex++) {
            VulkanRenderPass renderPass = renderPassList.get(renderPassIndex);

            List<VulkanAttachment> transformedAttachments = new ArrayList<>();
            @enumtype(VkImageLayout.class) List<Integer> oldLayout = new ArrayList<>();
            @enumtype(VkImageLayout.class) List<Integer> newLayout = new ArrayList<>();

            // 将所有输入附件的布局转换为可供组合图像采样器使用的布局
            for (VulkanAttachment inputAttachment : renderPass.inputAttachments) {
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
            for (RenderPassAttachmentInfo colorAttachmentInfo : renderPass.info.colorAttachmentInfos) {
                VulkanAttachment attachment = (VulkanAttachment) colorAttachmentInfo.attachment;

                @enumtype(VkImageLayout.class) int currentLayout = currentLayouts.getOrDefault(
                        attachment,
                        VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED
                );

                if (currentLayout != VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
                    && colorAttachmentInfo.clearBehavior == ClearBehavior.CLEAR_ALWAYS) {
                    // 如果确定需要清除附件，则起始布局不重要
                    currentLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
                }


                if (currentLayout != VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL) {
                    transformedAttachments.add(attachment);
                    oldLayout.add(currentLayout);
                    newLayout.add(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
                }

                currentLayouts.put(attachment, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            }

            if (!transformedAttachments.isEmpty()) {
                compiled.add(new ImageBarrierOp(engine.cx, transformedAttachments, oldLayout, newLayout));
            }

            // 计算附件是否需要被清除
            List<Boolean> colorAttachmentNeedClear = renderPass.info.colorAttachmentInfos.stream()
                    .map(info -> {
                        if (info.clearBehavior == ClearBehavior.CLEAR_ALWAYS) {
                            return true;
                        }

                        VulkanAttachment attachment = (VulkanAttachment) info.attachment;
                        return !attachmentCleared.contains(attachment);
                    })
                    .toList();
            attachmentCleared.addAll(renderPass.colorAttachments);

            boolean depthAttachmentNeedClear;
            if (renderPass.info.depthAttachmentInfo instanceof Option.Some<RenderPassAttachmentInfo> some) {
                VulkanAttachment attachment = (VulkanAttachment) some.value.attachment;
                depthAttachmentNeedClear = some.value.clearBehavior == ClearBehavior.CLEAR_ALWAYS
                        || !attachmentCleared.contains(attachment);
                attachmentCleared.add(attachment);
            } else {
                depthAttachmentNeedClear = false;
            }

            final int index = renderPassIndex;
            List<Boolean> colorAttachmentUsedInFuture = renderPass.colorAttachments.stream()
                    .map(att -> attachmentWriteUsedInTheFuture(att, renderPassList, index))
                    .toList();
            attachmentCleared.addAll(renderPass.colorAttachments);

            boolean depthAttachmentUsedInFuture;
            if (renderPass.depthAttachment instanceof Option.Some<VulkanImageAttachment> some) {
                depthAttachmentUsedInFuture = attachmentWriteUsedInTheFuture(some.value, renderPassList, index);
                attachmentCleared.add(some.value);
            } else {
                depthAttachmentUsedInFuture = false;
            }

            compiled.add(new RenderingBeginOp(
                    engine.cx,
                    renderPass,

                    colorAttachmentNeedClear,
                    colorAttachmentUsedInFuture,
                    depthAttachmentNeedClear,
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

    private boolean attachmentWriteUsedInTheFuture(
            VulkanAttachment attachment,
            List<VulkanRenderPass> renderPasses,
            int currentPassIndex
    ) {
        if (attachment == engine.swapchainColorAttachment) {
            return true;
        }

        for (int i = currentPassIndex + 1; i < renderPasses.size(); i++) {
            VulkanRenderPass renderPass = renderPasses.get(i);
            if (renderPass.inputAttachments.contains(attachment)) {
                return true;
            }

            for (RenderPassAttachmentInfo colorAttachmentInfo : renderPass.info.colorAttachmentInfos) {
                if (colorAttachmentInfo.attachment == attachment) {
                    return colorAttachmentInfo.clearBehavior != ClearBehavior.CLEAR_ALWAYS;
                }
            }

            if (renderPass.info.depthAttachmentInfo instanceof Option.Some<RenderPassAttachmentInfo> some) {
                if (some.value.attachment == attachment) {
                    return some.value.clearBehavior != ClearBehavior.CLEAR_ALWAYS;
                }
            }
        }

        return false;
    }

    // Reserved for future use maybe
    /*
    private static class AttachmentFutureUsageStat {
        public @Nullable RenderPass lastRenderPassAsInput = null;
        public ArrayList<RenderPass> futureNotUsedPasses = new ArrayList<>();

        private boolean pastLastRenderPassAsInput = false;

        // used for initialization
        private boolean isNextOneClearAlways = true;

        public void initWithOutputPassAndAttachment(RenderPass pass, ClearBehavior clearBehavior) {
            if (isNextOneClearAlways) {
                futureNotUsedPasses.add(pass);
            }
            isNextOneClearAlways = clearBehavior == ClearBehavior.CLEAR_ALWAYS;
        }

        public boolean isNotUsedInFuture(RenderPass renderPass) {
            if (lastRenderPassAsInput != null && lastRenderPassAsInput == renderPass) {
                pastLastRenderPassAsInput = true;
            }
            if (futureNotUsedPasses.isEmpty()) {
                return pastLastRenderPassAsInput;
            } else if (futureNotUsedPasses.getLast() == renderPass) {
                futureNotUsedPasses.removeLast();
            } else {
                return false;
            }
            return pastLastRenderPassAsInput;
        }
    }

    private HashMap<Attachment, AttachmentFutureUsageStat> getAttachmentFutureUsage() {
        HashMap<Attachment, AttachmentFutureUsageStat> futureUsage = new HashMap<>();
        List<VulkanRenderPass> passes = engine.renderPasses.stream().toList();
        for (int i = passes.size() - 1; i >= 0; i--) {
            VulkanRenderPass renderPass = passes.get(i);
            // input
            for (VulkanAttachment attachment : renderPass.inputAttachments) {
                futureUsage.compute(attachment, (_, value) -> {
                    if (value == null || value.lastRenderPassAsInput == null) {
                        value = new AttachmentFutureUsageStat();
                        value.lastRenderPassAsInput = renderPass;
                    }
                    return value;
                });
            }
            // output
            for (RenderPassAttachmentInfo attachmentInfo : renderPass.info.colorAttachmentInfos) {
                futureUsage.compute(attachmentInfo.attachment, (_, value) -> {
                    if (value == null) {
                        value = new AttachmentFutureUsageStat();
                    }
                    value.initWithOutputPassAndAttachment(renderPass, attachmentInfo.clearBehavior);
                    return value;
                });
            }
            if (renderPass.info.depthAttachmentInfo instanceof Option.Some<RenderPassAttachmentInfo> some) {
                futureUsage.compute(some.value.attachment, (_, value) -> {
                    if (value == null) {
                        value = new AttachmentFutureUsageStat();
                    }
                    value.initWithOutputPassAndAttachment(renderPass, some.value.clearBehavior);
                    return value;
                });
            }
        }
        return futureUsage;
    }
    */

    private static final Logger logger = Logger.getLogger(ASPECT_RenderPassCompilation.class.getName());
}
