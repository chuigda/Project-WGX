package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.task.AbstractPipelineBind;
import chr.wgx.render.task.AbstractRenderTaskGroup;
import chr.wgx.render.vk.data.VulkanRenderPipeline;

import java.util.HashMap;

public final class VulkanRenderPipelineBind extends AbstractPipelineBind {
    public final VulkanRenderPipeline pipeline;

    VulkanRenderPipelineBind(int priority, VulkanRenderPipeline pipeline) {
        super(priority);
        this.pipeline = pipeline;
    }

    @Override
    public synchronized AbstractRenderTaskGroup addRenderTaskGroup(
            HashMap<Integer, DescriptorSet> sharedDescriptorSets
    ) {
        return null;
    }
}
