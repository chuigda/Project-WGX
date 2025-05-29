package chr.wgx.render.common;

import club.doki7.ffm.annotation.EnumType;
import club.doki7.vulkan.bitmask.VkShaderStageFlags;

public enum ShaderStage {
    VERTEX(VkShaderStageFlags.VERTEX),
    FRAGMENT(VkShaderStageFlags.FRAGMENT),
    VERTEX_AND_FRAGMENT(VkShaderStageFlags.VERTEX | VkShaderStageFlags.FRAGMENT);

    @EnumType(VkShaderStageFlags.class) public final int vkShaderStageFlags;

    ShaderStage(@EnumType(VkShaderStageFlags.class) int vkShaderStageFlags) {
        this.vkShaderStageFlags = vkShaderStageFlags;
    }
}
