package chr.wgx.render.vk.task;

import chr.wgx.render.data.PushConstant;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanPushConstant;
import chr.wgx.render.vk.data.VulkanRenderObject;
import tech.icey.panama.buffer.LongBuffer;
import tech.icey.vk4j.handle.VkBuffer;
import tech.icey.vk4j.handle.VkDescriptorSet;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;

public final class VulkanRenderTask extends RenderTask {
    public final VulkanRenderObject renderObject;
    public final List<VulkanDescriptorSet> descriptorSets;
    public final Option<VulkanPushConstant> pushConstant;

    public final VkDescriptorSet.Buffer[] descriptorSetsVk;
    public final VkBuffer.Buffer pBuffer;
    public final LongBuffer pOffsets;

    VulkanRenderTask(
            VulkanRenderObject renderObject,
            List<VulkanDescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant,
            Arena prefabArena
    ) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets;
        this.pushConstant = pushConstant.map(pc -> (VulkanPushConstant) pc);

        int descriptorSetsPerFrame = descriptorSets.stream()
                .map(descriptorSet -> descriptorSet.descriptorSets.length)
                .max(Integer::compareTo)
                .orElse(1);
        this.descriptorSetsVk = new VkDescriptorSet.Buffer[descriptorSetsPerFrame];
        for (int i = 0; i < descriptorSetsPerFrame; i++) {
            this.descriptorSetsVk[i] = VkDescriptorSet.Buffer.allocate(prefabArena, descriptorSets.size());
            for (int j = 0; j < descriptorSets.size(); j++) {
                if (descriptorSets.get(j).descriptorSets.length == 1) {
                    // this descriptor set uses 1 vk descriptor set for all frames
                    this.descriptorSetsVk[i].write(j, descriptorSets.get(j).descriptorSets[0]);
                } else {
                    // this descriptor set uses multiple vk descriptor sets for different frames
                    this.descriptorSetsVk[i].write(j, descriptorSets.get(j).descriptorSets[i]);
                }
            }
        }

        this.pBuffer = VkBuffer.Buffer.allocate(prefabArena);
        this.pOffsets = LongBuffer.allocate(prefabArena);

        pBuffer.write(renderObject.vertexBuffer.buffer);
    }
}
