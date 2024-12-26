package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.task.RenderTaskGroup;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanRenderPipeline;

import java.lang.foreign.Arena;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class VulkanRenderPipelineBind extends RenderPipelineBind {
    public final VulkanRenderPipeline pipeline;
    public final ConcurrentLinkedQueue<VulkanRenderTaskGroup> renderTaskGroups = new ConcurrentLinkedQueue<>();

    private final Arena prefabArena;

    VulkanRenderPipelineBind(int priority, VulkanRenderPipeline pipeline, Arena prefabArena) {
        super(priority);
        this.pipeline = pipeline;

        this.prefabArena = prefabArena;
    }

    @Override
    public RenderTaskGroup createRenderTaskGroup(List<DescriptorSet> sharedDescriptorSets) {
        VulkanRenderTaskGroup ret = new VulkanRenderTaskGroup(
                sharedDescriptorSets.stream().map(ds -> (VulkanDescriptorSet) ds).toList(),
                prefabArena
        );
        renderTaskGroups.add(ret);
        return ret;
    }
}
