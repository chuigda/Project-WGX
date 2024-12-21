package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.info.DescriptorSetCreateInfo;
import chr.wgx.render.vk.data.VulkanDescriptorSet;
import chr.wgx.render.vk.data.VulkanDescriptorSetLayout;
import org.jetbrains.annotations.Nullable;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.datatype.VkDescriptorSetAllocateInfo;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkDescriptorPool;
import tech.icey.vk4j.handle.VkDescriptorSet;
import tech.icey.vk4j.handle.VkDescriptorSetLayout;

import java.lang.foreign.Arena;

public final class ASPECT_DescriptorSetCreate {
    ASPECT_DescriptorSetCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public VulkanDescriptorSet createDescriptorSetImpl(DescriptorSetCreateInfo createInfo) throws RenderException {
        if (!(createInfo.layout instanceof VulkanDescriptorSetLayout vulkanLayout)) {
            throw new IllegalArgumentException("DescriptorSetCreateInfo::layout 不是 VulkanDescriptorSetLayout, 是否错误地混用了不同的渲染引擎?");
        }

        @Nullable VkDescriptorPool descriptorPool = engine.descriptorPools.get(vulkanLayout);
        if (descriptorPool == null) {
            throw new IllegalArgumentException("未找到对应的 VkDescriptorPool, 是否未正确创建?");
        }

        VulkanRenderEngineContext cx = engine.cx;
        try (Arena arena = Arena.ofConfined()) {
            VkDescriptorSetLayout.Buffer pSetLayouts = VkDescriptorSetLayout.Buffer.allocate(arena);
            pSetLayouts.write(vulkanLayout.layout);

            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.allocate(arena);
            allocInfo.descriptorPool(descriptorPool);
            allocInfo.descriptorSetCount(1);
            allocInfo.pSetLayouts(pSetLayouts);

            VkDescriptorSet.Buffer pDescriptorSet = VkDescriptorSet.Buffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.dCmd.vkAllocateDescriptorSets(cx.device, allocInfo, pDescriptorSet);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法分配描述符集, 错误代码: " + VkResult.explain(result));
            }

            VulkanDescriptorSet ret = new VulkanDescriptorSet(createInfo, pDescriptorSet.read());
            engine.descriptorSets.add(ret);
            return ret;
        }
    }

    private final VulkanRenderEngine engine;
}
