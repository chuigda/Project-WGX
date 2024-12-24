package chr.wgx.render.vk.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.task.AbstractPipelineBindPoint;
import chr.wgx.render.task.AbstractRenderPass;
import chr.wgx.render.vk.data.VulkanAttachment;
import tech.icey.xjbutil.container.Option;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class VulkanRenderPass extends AbstractRenderPass {
    public final HashSet<VulkanAttachment> inputAttachments = new HashSet<>();
    public final HashSet<VulkanAttachment> outputAttachments = new HashSet<>();
    private final ConcurrentSkipListSet<VulkanRenderPipelineBindPoint> pipelineBindPoints = new ConcurrentSkipListSet<>();

    private final AtomicBoolean renderPassesNeedRecalculation;

    public VulkanRenderPass(
            String renderPassName,
            int priority,
            AtomicBoolean renderPassesNeedRecalculation
    ) {
        super(renderPassName, priority);
        this.renderPassesNeedRecalculation = renderPassesNeedRecalculation;
    }

    @Override
    public synchronized void addAttachments(
            List<Attachment> inputAttachments,
            List<Attachment> outputAttachments
    ) {
        for (Attachment attachment : inputAttachments) {
            this.inputAttachments.add((VulkanAttachment) attachment);
        }

        for (Attachment attachment : outputAttachments) {
            this.outputAttachments.add((VulkanAttachment) attachment);
        }

        renderPassesNeedRecalculation.set(true);
    }

    @Override
    public synchronized AbstractPipelineBindPoint addPipelineBindPoint(
            int priority,
            RenderPipeline pipeline,
            List<Attachment> colorAttachments,
            Option<Attachment> depthAttachment
    ) {
        return null;
    }
}
