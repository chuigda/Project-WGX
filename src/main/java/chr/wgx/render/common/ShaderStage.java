package chr.wgx.render.common;

import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.bitmask.VkShaderStageFlags;

public enum ShaderStage {
    VERTEX(VkShaderStageFlags.VK_SHADER_STAGE_VERTEX_BIT),
    FRAGMENT(VkShaderStageFlags.VK_SHADER_STAGE_FRAGMENT_BIT),
    VERTEX_AND_FRAGMENT(VkShaderStageFlags.VK_SHADER_STAGE_VERTEX_BIT | VkShaderStageFlags.VK_SHADER_STAGE_FRAGMENT_BIT);

    @enumtype(VkShaderStageFlags.class) public final int vkShaderStageFlags;

    ShaderStage(@enumtype(VkShaderStageFlags.class) int vkShaderStageFlags) {
        this.vkShaderStageFlags = vkShaderStageFlags;
    }
}
