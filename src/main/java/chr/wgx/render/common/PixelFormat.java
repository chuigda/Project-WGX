package chr.wgx.render.common;

import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkFormat;

import static tech.icey.gles2.GLES2Constants.*;
import static tech.icey.vk4j.enumtype.VkFormat.VK_FORMAT_D32_SFLOAT;

public enum PixelFormat {
    RGBA8888_FLOAT(0 /* actual value depends on configuration */, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE),
    // TODO
    // R32_UNSIGNED(VkFormat.VK_FORMAT_R32_UINT, GLES2Constants.GL_RGBA, GLES2Constants.GL_RGBA),
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
