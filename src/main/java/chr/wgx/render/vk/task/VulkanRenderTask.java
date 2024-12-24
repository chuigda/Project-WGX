package chr.wgx.render.vk.task;

import chr.wgx.render.task.RenderTask;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanRenderObject;
import tech.icey.panama.buffer.LongBuffer;
import tech.icey.vk4j.handle.VkBuffer;
import tech.icey.vk4j.handle.VkDescriptorSet;

import java.lang.foreign.Arena;
import java.util.List;

public final class VulkanRenderTask extends RenderTask {
    public final VulkanRenderObject renderObject;
    public final List<VulkanDescriptorSet> descriptorSets;

    public final VkDescriptorSet.Buffer descriptorSetsVk;
    public final VkBuffer.Buffer pBuffer;
    public final LongBuffer pOffsets;

    VulkanRenderTask(VulkanRenderObject renderObject, List<VulkanDescriptorSet> descriptorSets, Arena prefabArena) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets;

        this.descriptorSetsVk = VkDescriptorSet.Buffer.allocate(prefabArena, descriptorSets.size());
        for (int i = 0; i < descriptorSets.size(); i++) {
            descriptorSetsVk.write(i, descriptorSets.get(i).descriptorSet);
        }

        this.pBuffer = VkBuffer.Buffer.allocate(prefabArena);
        this.pOffsets = LongBuffer.allocate(prefabArena);

        pBuffer.write(renderObject.vertexBuffer.buffer);
    }
}
