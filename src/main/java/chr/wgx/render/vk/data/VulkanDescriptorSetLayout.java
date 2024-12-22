package chr.wgx.render.vk.data;

import chr.wgx.render.data.DescriptorSetLayout;
import chr.wgx.render.info.DescriptorSetLayoutCreateInfo;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.VulkanRenderEngineContext;
import tech.icey.vk4j.handle.VkDescriptorSetLayout;

public final class VulkanDescriptorSetLayout extends DescriptorSetLayout implements IVkDisposable {
    public final VkDescriptorSetLayout layout;

    public VulkanDescriptorSetLayout(DescriptorSetLayoutCreateInfo info, VkDescriptorSetLayout layout) {
        super(info);
        this.layout = layout;
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        cx.dCmd.vkDestroyDescriptorSetLayout(cx.device, layout, null);
    }
}
