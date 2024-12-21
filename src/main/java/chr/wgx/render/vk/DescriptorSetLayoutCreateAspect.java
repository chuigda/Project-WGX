package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.info.DescriptorLayoutBindingInfo;
import chr.wgx.render.info.DescriptorSetLayoutCreateInfo;
import chr.wgx.render.vk.data.VulkanDescriptorSetLayout;
import tech.icey.vk4j.datatype.VkDescriptorSetLayoutBinding;
import tech.icey.vk4j.datatype.VkDescriptorSetLayoutCreateInfo;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkDescriptorSetLayout;

import java.lang.foreign.Arena;

public final class DescriptorSetLayoutCreateAspect {
    public DescriptorSetLayoutCreateAspect(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public VulkanDescriptorSetLayout createDescriptorSetLayoutImpl(DescriptorSetLayoutCreateInfo info) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;

        try (Arena arena = Arena.ofConfined()) {
            VkDescriptorSetLayoutBinding[] bindings = VkDescriptorSetLayoutBinding.allocate(arena, info.bindings.size());
            for (int i = 0; i < info.bindings.size(); i++) {
                DescriptorLayoutBindingInfo bindingInfo = info.bindings.get(i);
                VkDescriptorSetLayoutBinding binding = VkDescriptorSetLayoutBinding.allocate(arena);

                binding.binding(i);
                binding.descriptorType(bindingInfo.descriptorType.vkDescriptorType);
                binding.descriptorCount(bindingInfo.descriptorCount);
                binding.stageFlags(bindingInfo.stage.vkShaderStageFlags);
            }

            VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.allocate(arena);
            createInfo.bindingCount(info.bindings.size());
            createInfo.pBindings(bindings[0]);

            VkDescriptorSetLayout.Buffer pDescriptorSetLayout = VkDescriptorSetLayout.Buffer.allocate(arena);

            int result = cx.dCmd.vkCreateDescriptorSetLayout(cx.device, createInfo, null, pDescriptorSetLayout);
            if (result != 0) {
                throw new RenderException("无法创建描述符集布局, 错误代码: " + VkResult.explain(result));
            }

            return new VulkanDescriptorSetLayout(info, pDescriptorSetLayout.read());
        }
    }

    private final VulkanRenderEngine engine;
}
