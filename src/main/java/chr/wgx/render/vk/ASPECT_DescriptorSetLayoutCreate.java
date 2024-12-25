package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.info.DescriptorLayoutBindingInfo;
import chr.wgx.render.info.DescriptorSetLayoutCreateInfo;
import chr.wgx.render.info.TextureBindingInfo;
import chr.wgx.render.info.UniformBufferBindingInfo;
import chr.wgx.render.vk.data.VulkanDescriptorSetLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.datatype.VkDescriptorPoolCreateInfo;
import tech.icey.vk4j.datatype.VkDescriptorPoolSize;
import tech.icey.vk4j.datatype.VkDescriptorSetLayoutBinding;
import tech.icey.vk4j.datatype.VkDescriptorSetLayoutCreateInfo;
import tech.icey.vk4j.enumtype.VkDescriptorType;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkDescriptorPool;
import tech.icey.vk4j.handle.VkDescriptorSetLayout;

import java.lang.foreign.Arena;

public final class ASPECT_DescriptorSetLayoutCreate {
    public ASPECT_DescriptorSetLayoutCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public VulkanDescriptorSetLayout createDescriptorSetLayoutImpl(
            DescriptorSetLayoutCreateInfo info,
            int maxDescriptorSetCount
    ) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;

        try (Arena arena = Arena.ofConfined()) {
            VkDescriptorSetLayoutBinding[] bindings = VkDescriptorSetLayoutBinding.allocate(arena, info.bindings.size());
            int uniformBufferCount = 0;
            int combinedImageSamplerCount = 0;
            for (int i = 0; i < info.bindings.size(); i++) {
                DescriptorLayoutBindingInfo bindingInfo = info.bindings.get(i);
                VkDescriptorSetLayoutBinding binding = bindings[i];

                binding.binding(i);
                binding.descriptorType(bindingInfo.descriptorType.vkDescriptorType);
                binding.descriptorCount(bindingInfo.descriptorCount);
                binding.stageFlags(bindingInfo.stage.vkShaderStageFlags);

                switch (bindingInfo) {
                    case TextureBindingInfo _ -> combinedImageSamplerCount += 1;
                    case UniformBufferBindingInfo _ -> uniformBufferCount += 1;
                }
            }

            VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.allocate(arena);
            createInfo.bindingCount(info.bindings.size());
            createInfo.pBindings(bindings[0]);

            VkDescriptorSetLayout.Buffer pDescriptorSetLayout = VkDescriptorSetLayout.Buffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.dCmd.vkCreateDescriptorSetLayout(
                    cx.device,
                    createInfo,
                    null,
                    pDescriptorSetLayout
            );
            if (result != 0) {
                throw new RenderException("无法创建描述符集布局, 错误代码: " + VkResult.explain(result));
            }

            VkDescriptorPoolSize[] descriptorPoolSize = VkDescriptorPoolSize.allocate(arena, 2);
            descriptorPoolSize[0].type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            descriptorPoolSize[0].descriptorCount(uniformBufferCount);
            descriptorPoolSize[1].type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            descriptorPoolSize[1].descriptorCount(combinedImageSamplerCount);

            VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.allocate(arena);
            descriptorPoolCreateInfo.maxSets(maxDescriptorSetCount);
            if (uniformBufferCount > 0 && combinedImageSamplerCount > 0) {
                descriptorPoolCreateInfo.poolSizeCount(2);
                descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize[0]);
            } else if (uniformBufferCount > 0) {
                descriptorPoolCreateInfo.poolSizeCount(1);
                descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize[0]);
            } else {
                descriptorPoolCreateInfo.poolSizeCount(1);
                descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize[1]);
            }

            VkDescriptorPool.Buffer pDescriptorPool = VkDescriptorPool.Buffer.allocate(arena);
            result = cx.dCmd.vkCreateDescriptorPool(cx.device, descriptorPoolCreateInfo, null, pDescriptorPool);
            if (result != 0) {
                throw new RenderException("无法创建描述符池, 错误代码: " + VkResult.explain(result));
            }

            VulkanDescriptorSetLayout ret = new VulkanDescriptorSetLayout(info, pDescriptorSetLayout.read());
            engine.descriptorPools.put(ret, pDescriptorPool.read());
            return ret;
        }
    }

    private final VulkanRenderEngine engine;
}
