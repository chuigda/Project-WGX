package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.task.RenderTaskGroup;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanRenderObject;
import tech.icey.vk4j.handle.VkDescriptorSet;

import java.lang.foreign.Arena;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class VulkanRenderTaskGroup extends RenderTaskGroup {
    public final ConcurrentLinkedQueue<VulkanRenderTask> renderTasks = new ConcurrentLinkedQueue<>();
    public final List<VulkanDescriptorSet> sharedDescriptorSets;
    public final VkDescriptorSet.Buffer sharedDescriptorSetsVk;

    private final Arena prefabArena;

    VulkanRenderTaskGroup(List<VulkanDescriptorSet> sharedDescriptorSets, Arena prefabArena) {
        this.prefabArena = prefabArena;

        this.sharedDescriptorSets = sharedDescriptorSets;
        this.sharedDescriptorSetsVk = VkDescriptorSet.Buffer.allocate(prefabArena, sharedDescriptorSets.size());
        for (int i = 0; i < sharedDescriptorSets.size(); i++) {
            sharedDescriptorSetsVk.write(i, sharedDescriptorSets.get(i).descriptorSet);
        }
    }

    @Override
    public RenderTask addRenderTask(RenderObject renderObject, List<DescriptorSet> descriptorSets) {
        VulkanRenderTask ret = new VulkanRenderTask(
                (VulkanRenderObject) renderObject,
                descriptorSets.stream().map(ds -> (VulkanDescriptorSet) ds).toList(),
                prefabArena
        );
        renderTasks.add(ret);
        return ret;
    }
}
