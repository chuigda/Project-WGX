package chr.wgx.render.common;

import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkFormat;

public enum PixelFormat {
    RGBA8888_FLOAT(0 /* actual value depends on configuration */),
    R16G16_FLOAT(VkFormat.VK_FORMAT_R16G16_SFLOAT),
    R32_UNSIGNED(VkFormat.VK_FORMAT_R32_UINT);

    public final @enumtype(VkFormat.class) int vkFormat;

    PixelFormat(@enumtype(VkFormat.class) int vkFormat) {
        this.vkFormat = vkFormat;
    }
}
