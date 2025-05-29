package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.info.DescriptorLayoutBindingInfo;
import chr.wgx.render.info.DescriptorSetLayoutCreateInfo;
import chr.wgx.render.info.TextureBindingInfo;
import chr.wgx.render.info.UniformBufferBindingInfo;
import chr.wgx.render.vk.data.VulkanDescriptorSetLayout;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.vulkan.datatype.VkDescriptorPoolCreateInfo;
import club.doki7.vulkan.datatype.VkDescriptorPoolSize;
import club.doki7.vulkan.datatype.VkDescriptorSetLayoutBinding;
import club.doki7.vulkan.datatype.VkDescriptorSetLayoutCreateInfo;
import club.doki7.vulkan.enumtype.VkDescriptorType;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vulkan.handle.VkDescriptorPool;
import club.doki7.vulkan.handle.VkDescriptorSetLayout;

import java.lang.foreign.Arena;
import java.util.Objects;

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
            VkDescriptorSetLayoutBinding.Ptr bindings = VkDescriptorSetLayoutBinding.allocate(arena, info.bindings.size());
            int uniformBufferCount = 0;
            int combinedImageSamplerCount = 0;
            for (int i = 0; i < info.bindings.size(); i++) {
                DescriptorLayoutBindingInfo bindingInfo = info.bindings.get(i);
                VkDescriptorSetLayoutBinding binding = bindings.at(i);

                binding.binding(i);
                binding.descriptorType(bindingInfo.descriptorType.vkDescriptorType);
                binding.descriptorCount(1);
                binding.stageFlags(bindingInfo.stage.vkShaderStageFlags);

                switch (bindingInfo) {
                    case TextureBindingInfo _ -> combinedImageSamplerCount += 1;
                    case UniformBufferBindingInfo _ -> uniformBufferCount += 1;
                }
            }

            VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.allocate(arena);
            createInfo.bindingCount(info.bindings.size());
            createInfo.pBindings(bindings);

            VkDescriptorSetLayout.Ptr pDescriptorSetLayout = VkDescriptorSetLayout.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = cx.dCmd.createDescriptorSetLayout(
                    cx.device,
                    createInfo,
                    null,
                    pDescriptorSetLayout
            );
            if (result != 0) {
                throw new RenderException("无法创建描述符集布局, 错误代码: " + VkResult.explain(result));
            }

            VkDescriptorPoolSize.Ptr descriptorPoolSize = VkDescriptorPoolSize.allocate(arena, 2);
            descriptorPoolSize.at(0).type(VkDescriptorType.UNIFORM_BUFFER);
            descriptorPoolSize.at(0).descriptorCount(uniformBufferCount * maxDescriptorSetCount);
            descriptorPoolSize.at(1).type(VkDescriptorType.COMBINED_IMAGE_SAMPLER);
            descriptorPoolSize.at(1).descriptorCount(combinedImageSamplerCount * maxDescriptorSetCount);

            VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.allocate(arena);
            descriptorPoolCreateInfo.maxSets(maxDescriptorSetCount);
            if (uniformBufferCount > 0 && combinedImageSamplerCount > 0) {
                descriptorPoolCreateInfo.poolSizeCount(2);
                descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize);
            } else if (uniformBufferCount > 0) {
                descriptorPoolCreateInfo.poolSizeCount(1);
                descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize);
            } else {
                descriptorPoolCreateInfo.poolSizeCount(1);
                descriptorPoolCreateInfo.pPoolSizes(descriptorPoolSize);
            }

            VkDescriptorPool.Ptr pDescriptorPool = VkDescriptorPool.Ptr.allocate(arena);
            result = cx.dCmd.createDescriptorPool(cx.device, descriptorPoolCreateInfo, null, pDescriptorPool);
            if (result != 0) {
                throw new RenderException("无法创建描述符池, 错误代码: " + VkResult.explain(result));
            }

            VulkanDescriptorSetLayout ret = new VulkanDescriptorSetLayout(
                    info,
                    Objects.requireNonNull(pDescriptorSetLayout.read())
            );
            engine.descriptorPools.put(
                    ret,
                    Objects.requireNonNull(pDescriptorPool.read())
            );
            return ret;
        }
    }

    private final VulkanRenderEngine engine;
}
