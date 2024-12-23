package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.task.AbstractPipelineBindPoint;
import chr.wgx.render.task.AbstractRenderTaskGroup;
import chr.wgx.render.vk.data.VulkanAttachment;
import tech.icey.xjbutil.container.Option;

import java.util.HashMap;
import java.util.List;

public final class VulkanRenderPipelineBindPoint extends AbstractPipelineBindPoint {
    public final List<VulkanAttachment> colorAttachments;
    public final Option<VulkanAttachment> depthAttachment;

    VulkanRenderPipelineBindPoint(
            int priority,
            RenderPipeline pipeline,
            List<VulkanAttachment> colorAttachments,
            Option<VulkanAttachment> depthAttachment
    ) {
        super(priority, pipeline);

        this.colorAttachments = colorAttachments;
        this.depthAttachment = depthAttachment;
    }

    @Override
    public synchronized AbstractRenderTaskGroup addRenderTaskGroup(
            HashMap<Integer, DescriptorSet> sharedDescriptorSets
    ) {
        return null;
    }
}
