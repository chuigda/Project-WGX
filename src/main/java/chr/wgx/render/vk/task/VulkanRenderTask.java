package chr.wgx.render.vk.task;

import chr.wgx.render.data.PushConstant;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanPushConstant;
import chr.wgx.render.vk.data.VulkanRenderObject;
import club.doki7.ffm.ptr.LongPtr;
import club.doki7.vulkan.handle.VkBuffer;
import club.doki7.vulkan.handle.VkDescriptorSet;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;

public final class VulkanRenderTask extends RenderTask {
    public final VulkanRenderObject renderObject;
    public final List<VulkanDescriptorSet> descriptorSets;
    public final Option<VulkanPushConstant> pushConstant;

    public final VkDescriptorSet.Ptr[] descriptorSetsVk;
    public final VkBuffer.Ptr pBuffer;
    public final LongPtr pOffsets;

    VulkanRenderTask(
            VulkanRenderObject renderObject,
            List<VulkanDescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant,
            Arena prefabArena
    ) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets;
        this.pushConstant = pushConstant.map(pc -> (VulkanPushConstant) pc);

        int descriptorSetsPerFrame = (int) (long) descriptorSets.stream()
                .map(descriptorSet -> descriptorSet.descriptorSets.size())
                .max(Long::compareTo)
                .orElse(1L);
        this.descriptorSetsVk = new VkDescriptorSet.Ptr[descriptorSetsPerFrame];
        for (int i = 0; i < descriptorSetsPerFrame; i++) {
            this.descriptorSetsVk[i] = VkDescriptorSet.Ptr.allocate(prefabArena, descriptorSets.size());
            for (int j = 0; j < descriptorSets.size(); j++) {
                if (descriptorSets.get(j).descriptorSets.size() == 1) {
                    // this descriptor set uses 1 vk descriptor set for all frames
                    this.descriptorSetsVk[i].write(j, descriptorSets.get(j).descriptorSets.read());
                } else {
                    // this descriptor set uses multiple vk descriptor sets for different frames
                    this.descriptorSetsVk[i].write(j, descriptorSets.get(j).descriptorSets.read(i));
                }
            }
        }

        this.pBuffer = VkBuffer.Ptr.allocate(prefabArena);
        this.pOffsets = LongPtr.allocate(prefabArena);

        pBuffer.write(renderObject.vertexBuffer.buffer);
    }
}
