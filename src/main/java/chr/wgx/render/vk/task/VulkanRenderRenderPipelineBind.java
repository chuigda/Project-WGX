package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.task.RenderTaskGroup;
import chr.wgx.render.vk.data.VulkanRenderPipeline;

import java.util.HashMap;

public final class VulkanRenderRenderPipelineBind extends RenderPipelineBind {
    public final VulkanRenderPipeline pipeline;

    VulkanRenderRenderPipelineBind(int priority, VulkanRenderPipeline pipeline) {
        super(priority);
        this.pipeline = pipeline;
    }

    @Override
    public synchronized RenderTaskGroup addRenderTaskGroup(
            HashMap<Integer, DescriptorSet> sharedDescriptorSets
    ) {
        return null;
    }
}
