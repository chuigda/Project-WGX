package chr.wgx.render.info;

import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkDescriptorType;

public enum DescriptorType {
    COMBINED_IMAGE_SAMPLER(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER),
    UNIFORM_BUFFER(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);

    public final @enumtype(VkDescriptorType.class) int vkDescriptorType;

    DescriptorType(@enumtype(VkDescriptorType.class) int vkDescriptorType) {
        this.vkDescriptorType = vkDescriptorType;
    }
}
