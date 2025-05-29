package chr.wgx.render.vk.data;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.info.DescriptorSetCreateInfo;
import club.doki7.vulkan.handle.VkDescriptorSet;

public final class VulkanDescriptorSet extends DescriptorSet {
    public final VkDescriptorSet.Ptr descriptorSets;

    public VulkanDescriptorSet(DescriptorSetCreateInfo createInfo, VkDescriptorSet.Ptr descriptorSets) {
        super(createInfo);
        this.descriptorSets = descriptorSets;
    }
}
