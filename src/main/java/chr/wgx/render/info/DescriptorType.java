package chr.wgx.render.info;

import club.doki7.ffm.annotation.EnumType;
import club.doki7.vulkan.enumtype.VkDescriptorType;

public enum DescriptorType {
    COMBINED_IMAGE_SAMPLER(VkDescriptorType.COMBINED_IMAGE_SAMPLER),
    UNIFORM_BUFFER(VkDescriptorType.UNIFORM_BUFFER);

    public final @EnumType(VkDescriptorType.class) int vkDescriptorType;

    DescriptorType(@EnumType(VkDescriptorType.class) int vkDescriptorType) {
        this.vkDescriptorType = vkDescriptorType;
    }
}
