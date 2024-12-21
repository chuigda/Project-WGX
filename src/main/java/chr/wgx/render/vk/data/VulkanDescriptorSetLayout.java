package chr.wgx.render.vk.data;

import chr.wgx.render.data.DescriptorSetLayout;
import chr.wgx.render.info.DescriptorSetLayoutCreateInfo;
import tech.icey.vk4j.handle.VkDescriptorSetLayout;

public final class VulkanDescriptorSetLayout extends DescriptorSetLayout {
    public final VkDescriptorSetLayout layout;

    public VulkanDescriptorSetLayout(DescriptorSetLayoutCreateInfo info, VkDescriptorSetLayout layout) {
        super(info);
        this.layout = layout;
    }
}
