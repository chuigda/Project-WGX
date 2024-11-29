package chr.wgx.render.common;

import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkFormat;

public enum CGType {
    Float (4,         4,  4, VkFormat.VK_FORMAT_R32_SFLOAT, 1),
    Vec2  (4 * 2,     4,  8, VkFormat.VK_FORMAT_R32G32_SFLOAT, 1),
    Vec3  (4 * 3,     4, 16, VkFormat.VK_FORMAT_R32G32B32_SFLOAT, 1),
    Vec4  (4 * 4,     4, 16, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, 1),
    Mat2  (2 * 2 * 4, 4, 16, VkFormat.VK_FORMAT_R32G32_SFLOAT, 2),
    Mat3  (3 * 3 * 4, 4, 16, VkFormat.VK_FORMAT_R32G32B32_SFLOAT, 3),
    Mat4  (4 * 4 * 4, 4, 16, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, 4);

    /// 数据类型在 CPU 中存储时消耗的字节数
    public final int byteSize;
    /// 数据类型在 C ABI 中的对齐
    public final int alignment;
    /// 数据类型在 std140 布局中的对齐
    public final int std140Alignment;
    /// 对应的 Vulkan format
    public final @enumtype(VkFormat.class) int vkFormat;
    /// 在 GLES 中占用的 index 数量
    public final int glIndexSize;

    CGType(int byteSize, int alignment, int std140Alignment, @enumtype(VkFormat.class) int vkFormat, int glIndexSize) {
        this.byteSize = byteSize;
        this.alignment = alignment;
        this.std140Alignment = std140Alignment;
        this.vkFormat = vkFormat;
        this.glIndexSize = glIndexSize;
    }
}
