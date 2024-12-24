package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.task.RenderTaskGroup;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanRenderObject;

import java.lang.foreign.Arena;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class VulkanRenderTaskGroup extends RenderTaskGroup {
    public final ConcurrentLinkedQueue<VulkanRenderTask> renderTasks = new ConcurrentLinkedQueue<>();

    private final Arena prefabArena;

    VulkanRenderTaskGroup(List<DescriptorSet> sharedDescriptorSets, Arena prefabArena) {
        this.prefabArena = prefabArena;
    }

    @Override
    public RenderTask addRenderTask(RenderObject renderObject, List<DescriptorSet> descriptorSets) {
        VulkanRenderTask ret = new VulkanRenderTask(
                (VulkanRenderObject) renderObject,
                descriptorSets.stream().map(ds -> (VulkanDescriptorSet) ds).toList()
        );
        renderTasks.add(ret);
        return ret;
    }
}
