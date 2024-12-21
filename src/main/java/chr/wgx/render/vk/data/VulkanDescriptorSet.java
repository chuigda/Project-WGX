package chr.wgx.render.vk.data;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.info.DescriptorSetCreateInfo;
import tech.icey.vk4j.handle.VkDescriptorSet;

public final class VulkanDescriptorSet extends DescriptorSet {
    public final VkDescriptorSet descriptorSet;

    public VulkanDescriptorSet(DescriptorSetCreateInfo createInfo, VkDescriptorSet descriptorSet) {
        super(createInfo);
        this.descriptorSet = descriptorSet;
    }
}
