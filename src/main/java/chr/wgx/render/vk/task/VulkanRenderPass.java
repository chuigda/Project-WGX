package chr.wgx.render.vk.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.task.RenderPass;
import chr.wgx.render.vk.data.VulkanAttachment;
import chr.wgx.render.vk.data.VulkanImageAttachment;
import chr.wgx.render.vk.data.VulkanRenderPipeline;
import tech.icey.xjbutil.container.Option;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VulkanRenderPass extends RenderPass {
    public final List<VulkanAttachment> colorAttachments;
    public final Option<VulkanImageAttachment> depthAttachment;

    public final HashSet<VulkanAttachment> inputAttachments = new HashSet<>();
    public final ConcurrentSkipListSet<VulkanRenderRenderPipelineBind> bindList = new ConcurrentSkipListSet<>();

    private final AtomicBoolean renderPassesNeedRecalculation;

    public VulkanRenderPass(
            String renderPassName,
            int priority,
            List<VulkanAttachment> colorAttachments,
            Option<VulkanImageAttachment> depthAttachment,
            AtomicBoolean renderPassesNeedRecalculation
    ) {
        super(renderPassName, priority);
        this.colorAttachments = colorAttachments;
        this.depthAttachment = depthAttachment;
        this.renderPassesNeedRecalculation = renderPassesNeedRecalculation;

        renderPassesNeedRecalculation.set(true);
    }

    @Override
    public synchronized void addInputAttachments(List<Attachment> attachments) {
        for (Attachment attachment : attachments) {
            inputAttachments.add((VulkanAttachment) attachment);
        }

        renderPassesNeedRecalculation.set(true);
    }

    @Override
    public RenderPipelineBind addPipelineBindPoint(int priority, RenderPipeline pipeline) {
        VulkanRenderRenderPipelineBind bind = new VulkanRenderRenderPipelineBind(priority, (VulkanRenderPipeline) pipeline);
        bindList.add(bind);
        return bind;
    }
}
