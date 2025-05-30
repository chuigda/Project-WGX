package chr.wgx.render.common;

import club.doki7.gles2.GLES2Constants;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.vulkan.enumtype.VkFormat;

import static chr.wgx.render.gles2.glext.EXT_texture_storage.GL_R32F_EXT;
import static club.doki7.gles2.GLES2Constants.*;
import static club.doki7.vulkan.enumtype.VkFormat.D32_SFLOAT;

public enum PixelFormat {
    /// 供一般彩色附件和纹理使用的格式，这个格式也非常方便从 CPU 上传 RGBA 数据到 GPU
    RGBA_OPTIMAL(VkFormat.R8G8B8A8_SRGB, RGBA, RGBA, UNSIGNED_BYTE),
    /// 交换链图像使用的格式，使用对于 Vulkan 而言支持更加广泛的 BGRA 而不是 RGBA 排列
    RGBA_SWAPCHAIN(VkFormat.B8G8R8A8_SRGB, RGBA, RGBA, UNSIGNED_BYTE),
    /// 单色纹理使用的格式
    R32_FLOAT(VkFormat.R32_SFLOAT, GL_R32F_EXT, GL_R32F_EXT, FLOAT),
    /// 用于存储 ID 类数据的纹理使用的格式；GLES2 不支持单通道无符号整数纹理，仍然使用浮点纹理
    R32_UINT(VkFormat.R32_UINT, GL_R32F_EXT, GL_R32F_EXT, FLOAT),
    /// 深度缓冲附件使用的格式
    DEPTH_BUFFER_OPTIMAL(D32_SFLOAT, DEPTH_COMPONENT, DEPTH_COMPONENT, UNSIGNED_INT);

    public final @EnumType(VkFormat.class) int vkFormat;
    public final @EnumType(GLES2Constants.class) int glFormat;
    public final @EnumType(GLES2Constants.class) int glInternalFormat;
    public final @EnumType(GLES2Constants.class) int glType;

    PixelFormat(
            @EnumType(VkFormat.class) int vkFormat,
            @EnumType(GLES2Constants.class) int glFormat,
            @EnumType(GLES2Constants.class) int glInternalFormat,
            @EnumType(GLES2Constants.class) int glType
    ) {
        this.vkFormat = vkFormat;

        this.glFormat = glFormat;
        this.glInternalFormat = glInternalFormat;
        this.glType = glType;
    }
}
