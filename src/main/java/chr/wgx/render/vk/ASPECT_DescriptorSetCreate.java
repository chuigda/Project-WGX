package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.Descriptor;
import chr.wgx.render.data.Texture;
import chr.wgx.render.data.UniformBuffer;
import chr.wgx.render.info.DescriptorSetCreateInfo;
import chr.wgx.render.vk.data.CombinedImageSampler;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanDescriptorSetLayout;
import chr.wgx.render.vk.data.VulkanUniformBuffer;
import org.jetbrains.annotations.Nullable;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.datatype.VkDescriptorBufferInfo;
import club.doki7.vulkan.datatype.VkDescriptorImageInfo;
import club.doki7.vulkan.datatype.VkDescriptorSetAllocateInfo;
import club.doki7.vulkan.datatype.VkWriteDescriptorSet;
import club.doki7.vulkan.enumtype.VkDescriptorType;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vulkan.handle.VkDescriptorPool;
import club.doki7.vulkan.handle.VkDescriptorSet;
import club.doki7.vulkan.handle.VkDescriptorSetLayout;
import tech.icey.xjbutil.container.Pair;

import java.lang.foreign.Arena;
import java.util.*;
import java.util.logging.Logger;

public final class ASPECT_DescriptorSetCreate {
    ASPECT_DescriptorSetCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    VulkanDescriptorSet createDescriptorSetImpl(DescriptorSetCreateInfo createInfo) throws RenderException {
        if (!(createInfo.layout instanceof VulkanDescriptorSetLayout vulkanLayout)) {
            throw new IllegalArgumentException("DescriptorSetCreateInfo::layout 不是 VulkanDescriptorSetLayout, 是否错误地混用了不同的渲染引擎?");
        }

        int frameDescriptorSets = createInfo.descriptors.stream()
                .map(descriptor -> switch (descriptor) {
                    case Texture _ -> 1;
                    case UniformBuffer uniformBuffer -> ((VulkanUniformBuffer) uniformBuffer).underlyingBuffer.size();
                })
                .max(Integer::compareTo)
                .orElse(1);

        @Nullable VkDescriptorPool descriptorPool = engine.descriptorPools.get(vulkanLayout);
        if (descriptorPool == null) {
            throw new IllegalArgumentException("未找到对应的 VkDescriptorPool, 是否未正确创建?");
        }

        VulkanRenderEngineContext cx = engine.cx;
        try (Arena arena = Arena.ofConfined()) {
            VkDescriptorSetLayout.Ptr pSetLayouts = VkDescriptorSetLayout.Ptr.allocate(arena, frameDescriptorSets);
            for (int i = 0; i < frameDescriptorSets; i++) {
                pSetLayouts.write(i, vulkanLayout.layout);
            }

            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.allocate(arena);
            allocInfo.descriptorPool(descriptorPool);
            allocInfo.descriptorSetCount(frameDescriptorSets);
            allocInfo.pSetLayouts(pSetLayouts);

            VkDescriptorSet.Ptr pDescriptorSets = VkDescriptorSet.Ptr.allocate(arena, frameDescriptorSets);
            @EnumType(VkResult.class) int result = cx.dCmd.allocateDescriptorSets(cx.device, allocInfo, pDescriptorSets);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法分配描述符集, 错误代码: " + VkResult.explain(result));
            }

            VkWriteDescriptorSet.Ptr descriptorSetWrites = VkWriteDescriptorSet.allocate(arena, createInfo.descriptors.size());
            List<Pair<CombinedImageSampler, Integer>> usedCombinedImageSamplers = new ArrayList<>();
            for (int i = 0; i < frameDescriptorSets; i++) {
                VkDescriptorSet descriptorSet = Objects.requireNonNull(pDescriptorSets.read(i));

                for (int j = 0; j < createInfo.descriptors.size(); j++) {
                    Descriptor descriptor = createInfo.descriptors.get(j);
                    VkWriteDescriptorSet write = descriptorSetWrites.at(j);

                    write.dstSet(descriptorSet);
                    write.dstBinding(j);
                    write.dstArrayElement(0);
                    write.descriptorCount(1);

                    switch (descriptor) {
                        case Texture texture -> {
                            write.descriptorType(VkDescriptorType.COMBINED_IMAGE_SAMPLER);

                            CombinedImageSampler combinedImageSampler = (CombinedImageSampler) texture;
                            VkDescriptorImageInfo imageInfo = VkDescriptorImageInfo.allocate(arena);
                            imageInfo.imageLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL);
                            imageInfo.imageView(combinedImageSampler.image.value.imageView);
                            imageInfo.sampler(combinedImageSampler.sampler.sampler);

                            write.pImageInfo(imageInfo);
                            usedCombinedImageSamplers.add(new Pair<>(combinedImageSampler, j));
                        }
                        case UniformBuffer uniformBuffer -> {
                            write.descriptorType(VkDescriptorType.UNIFORM_BUFFER);

                            VulkanUniformBuffer vulkanUniformBuffer = (VulkanUniformBuffer) uniformBuffer;
                            VkDescriptorBufferInfo bufferInfo = VkDescriptorBufferInfo.allocate(arena);
                            if (vulkanUniformBuffer.underlyingBuffer.size() == 1) {
                                // this uniform buffer uses 1 vk buffer for all frames
                                bufferInfo.buffer(vulkanUniformBuffer.underlyingBuffer.getFirst().buffer);
                            } else {
                                // this uniform buffer uses multiple vk buffers for different frames
                                bufferInfo.buffer(vulkanUniformBuffer.underlyingBuffer.get(i).buffer);
                            }
                            bufferInfo.range(VkConstants.WHOLE_SIZE);

                            write.pBufferInfo(bufferInfo);
                        }
                    }
                }

                cx.dCmd.updateDescriptorSets(cx.device, createInfo.descriptors.size(), descriptorSetWrites, 0, null);
            }

            VulkanDescriptorSet ret = new VulkanDescriptorSet(createInfo, pDescriptorSets);
            synchronized (engine.imageDescriptorSetUsage) {
                for (Pair<CombinedImageSampler, Integer> pair : usedCombinedImageSamplers) {
                    Set<VulkanRenderEngine.ImageDescriptorUsage> s = engine.imageDescriptorSetUsage.computeIfAbsent(
                            pair.first().image,
                            _ -> new HashSet<>()
                    );

                    VulkanRenderEngine.ImageDescriptorUsage usage = new VulkanRenderEngine.ImageDescriptorUsage(
                            ret,
                            pair.second()
                    );
                    s.add(usage);
                }
            }

            engine.descriptorSets.add(ret);
            return ret;
        }
    }

    void updateDescriptorSetItem(Set<VulkanRenderEngine.ImageDescriptorUsage> usages) {
        VulkanRenderEngineContext cx = engine.cx;

        try (Arena arena = Arena.ofConfined()) {
            for (VulkanRenderEngine.ImageDescriptorUsage usage : usages) {
                VulkanDescriptorSet descriptorSet = usage.descriptorSet;
                Descriptor descriptor = descriptorSet.createInfo.descriptors.get(usage.binding);

                if (!(descriptor instanceof CombinedImageSampler combinedImageSampler)) {
                    assert false;
                    logger.warning("描述符集合中 binding=" + usage.binding + " 的描述符不是纹理，已跳过");
                    continue;
                }

                VkWriteDescriptorSet.Ptr write = VkWriteDescriptorSet.allocate(arena, descriptorSet.descriptorSets.size());
                for (int i = 0; i < descriptorSet.descriptorSets.size(); i++) {
                    VkWriteDescriptorSet writeDescriptor = write.at(i);
                    writeDescriptor.dstSet(descriptorSet.descriptorSets.read(i));
                    writeDescriptor.dstBinding(usage.binding);
                    writeDescriptor.dstArrayElement(0);
                    writeDescriptor.descriptorCount(1);
                    writeDescriptor.descriptorType(VkDescriptorType.COMBINED_IMAGE_SAMPLER);

                    VkDescriptorImageInfo imageInfo = VkDescriptorImageInfo.allocate(arena);
                    imageInfo.imageLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL);
                    imageInfo.imageView(combinedImageSampler.image.value.imageView);
                    imageInfo.sampler(combinedImageSampler.sampler.sampler);

                    writeDescriptor.pImageInfo(imageInfo);
                }

                cx.dCmd.updateDescriptorSets(cx.device, (int) descriptorSet.descriptorSets.size(), write, 0, null);
            }
        }
    }

    private final VulkanRenderEngine engine;

    private static final Logger logger = Logger.getLogger(ASPECT_DescriptorSetCreate.class.getName());
}
