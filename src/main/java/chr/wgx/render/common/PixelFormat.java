package chr.wgx.render.common;

import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkFormat;

import static tech.icey.gles2.GLES2Constants.*;
import static tech.icey.vk4j.enumtype.VkFormat.VK_FORMAT_D32_SFLOAT;
import static chr.wgx.render.gles2.glext.EXT_texture_storage.GL_R32F_EXT;

public enum PixelFormat {
    /// 供一般彩色附件和纹理使用的格式，这个格式也非常方便从 CPU 上传 RGBA 数据到 GPU
    RGBA_OPTIMAL(VkFormat.VK_FORMAT_R8G8B8A8_SRGB, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE),
    /// 交换链图像使用的格式，使用对于 Vulkan 而言支持更加广泛的 BGRA 而不是 RGBA 排列
    RGBA_SWAPCHAIN(VkFormat.VK_FORMAT_B8G8R8A8_SRGB, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE),
    /// 单色纹理使用的格式
    R32_FLOAT(VkFormat.VK_FORMAT_R32_SFLOAT, GL_R32F_EXT, GL_R32F_EXT, GL_FLOAT),
    /// 用于存储 ID 类数据的纹理使用的格式；GLES2 不支持单通道无符号整数纹理，仍然使用浮点纹理
    R32_UINT(VkFormat.VK_FORMAT_R32_UINT, GL_R32F_EXT, GL_R32F_EXT, GL_FLOAT),
    /// 深度缓冲附件使用的格式
    DEPTH_BUFFER_OPTIMAL(VK_FORMAT_D32_SFLOAT, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT);

    public final @enumtype(VkFormat.class) int vkFormat;
    public final @enumtype(GLES2Constants.class) int glFormat;
    public final @enumtype(GLES2Constants.class) int glInternalFormat;
    public final @enumtype(GLES2Constants.class) int glType;

    PixelFormat(
            @enumtype(VkFormat.class) int vkFormat,
            @enumtype(GLES2Constants.class) int glFormat,
            @enumtype(GLES2Constants.class) int glInternalFormat,
            @enumtype(GLES2Constants.class) int glType
    ) {
        this.vkFormat = vkFormat;

        this.glFormat = glFormat;
        this.glInternalFormat = glInternalFormat;
        this.glType = glType;
    }
}
