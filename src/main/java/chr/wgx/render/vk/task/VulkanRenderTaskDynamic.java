package chr.wgx.render.vk.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.PushConstant;
import chr.wgx.render.task.RenderTaskDynamic;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanPushConstant;
import chr.wgx.render.vk.data.VulkanRenderObject;
import tech.icey.panama.buffer.LongBuffer;
import tech.icey.vk4j.handle.VkBuffer;
import tech.icey.vk4j.handle.VkDescriptorSet;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class VulkanRenderTaskDynamic extends RenderTaskDynamic {
    public final VulkanRenderObject renderObject;
    public final List<AtomicReference<VulkanDescriptorSet>> descriptorSets;
    public final Option<VulkanPushConstant> pushConstant;

    public final VkDescriptorSet.Buffer descriptorSetsVk;
    public final VkBuffer.Buffer pBuffer;
    public final LongBuffer pOffsets;

    VulkanRenderTaskDynamic(
            VulkanRenderObject renderObject,
            List<VulkanDescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant,
            Arena prefabArena
    ) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets.stream()
                .map(AtomicReference::new)
                .toList();
        this.pushConstant = pushConstant.map(pc -> (VulkanPushConstant) pc);

        int descriptorSetsPerFrame = descriptorSets.stream()
                .map(descriptorSet -> descriptorSet.descriptorSets.length)
                .max(Integer::compareTo)
                .orElse(1);

        // 因为描述符集合是动态变化的，因此不需要预制描述符集合
        // 现在这个只是用于避免在录制指令缓冲时分配内存
        this.descriptorSetsVk = VkDescriptorSet.Buffer.allocate(prefabArena, descriptorSets.size());

        this.pBuffer = VkBuffer.Buffer.allocate(prefabArena);
        this.pOffsets = LongBuffer.allocate(prefabArena);

        pBuffer.write(renderObject.vertexBuffer.buffer);
    }

    @Override
    public void updateDescriptorSet(int location, DescriptorSet descriptorSet) {
        this.descriptorSets.get(location).set((VulkanDescriptorSet) descriptorSet);
    }
}
