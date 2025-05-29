package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.PushConstant;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.task.RenderTaskDynamic;
import chr.wgx.render.task.RenderTaskGroup;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanRenderObject;
import club.doki7.vulkan.handle.VkDescriptorSet;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class VulkanRenderTaskGroup extends RenderTaskGroup {
    public final ConcurrentLinkedQueue<VulkanRenderTask> renderTasks = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<VulkanRenderTaskDynamic> dynamicRenderTasks = new ConcurrentLinkedQueue<>();
    public final List<VulkanDescriptorSet> sharedDescriptorSets;
    public final VkDescriptorSet.Ptr[] sharedDescriptorSetsVk;

    private final Arena prefabArena;

    VulkanRenderTaskGroup(List<VulkanDescriptorSet> sharedDescriptorSets, Arena prefabArena) {
        this.prefabArena = prefabArena;

        this.sharedDescriptorSets = sharedDescriptorSets;
        int descriptorSetsPerFrame = (int) (long) (sharedDescriptorSets.stream()
                .map(descriptorSet -> descriptorSet.descriptorSets.size())
                .max(Long::compareTo)
                .orElse(1L));

        this.sharedDescriptorSetsVk = new VkDescriptorSet.Ptr[descriptorSetsPerFrame];
        for (int i = 0; i < descriptorSetsPerFrame; i++) {
            this.sharedDescriptorSetsVk[i] = VkDescriptorSet.Ptr.allocate(prefabArena, sharedDescriptorSets.size());
            for (int j = 0; j < sharedDescriptorSets.size(); j++) {
                if (sharedDescriptorSets.get(j).descriptorSets.size() == 1) {
                    // this descriptor set uses 1 vk descriptor set for all frames
                    this.sharedDescriptorSetsVk[i].write(j, sharedDescriptorSets.get(j).descriptorSets.read(0));
                } else {
                    // this descriptor set uses multiple vk descriptor sets for different frames
                    this.sharedDescriptorSetsVk[i].write(j, sharedDescriptorSets.get(j).descriptorSets.read(i));
                }
            }
        }
    }

    @Override
    public RenderTask addRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant
    ) {
        VulkanRenderTask ret = new VulkanRenderTask(
                (VulkanRenderObject) renderObject,
                descriptorSets.stream().map(ds -> (VulkanDescriptorSet) ds).toList(),
                pushConstant,
                prefabArena
        );
        renderTasks.add(ret);
        return ret;
    }

    @Override
    public RenderTaskDynamic addDynamicRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant
    ) {
        VulkanRenderTaskDynamic ret = new VulkanRenderTaskDynamic(
                (VulkanRenderObject) renderObject,
                descriptorSets.stream().map(ds -> (VulkanDescriptorSet) ds).toList(),
                pushConstant,
                prefabArena
        );
        dynamicRenderTasks.add(ret);
        return ret;
    }
}
