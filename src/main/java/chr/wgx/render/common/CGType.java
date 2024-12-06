package chr.wgx.render.common;

import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkFormat;

public enum CGType {
    Float (1, 4,  4, VkFormat.VK_FORMAT_R32_SFLOAT, false),
    Vec2  (2, 4,  8, VkFormat.VK_FORMAT_R32G32_SFLOAT, false),
    Vec3  (3, 4, 16, VkFormat.VK_FORMAT_R32G32B32_SFLOAT, false),
    Vec4  (4, 4, 16, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, false),
    Int   (1, 4,  4, VkFormat.VK_FORMAT_R32_SINT, false),
    Mat2  (2, 4, 16, VkFormat.VK_FORMAT_R32G32_SFLOAT, true),
    Mat3  (3, 4, 16, VkFormat.VK_FORMAT_R32G32B32_SFLOAT, true),
    Mat4  (4, 4, 16, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, true);

    private static final int FLOAT_SIZE = 4;

    /// 数据类型在 CPU 中存储时消耗的字节数
    public final int byteSize;
    /// 数据类型在 C ABI 中的对齐
    public final int alignment;
    /// 数据类型在 std140 布局中的对齐
    public final int std140Alignment;
    /// 对应的 Vulkan format
    public final @enumtype(VkFormat.class) int vkFormat;
    /// 在 GLES 中占用的 index 数量, 这也等价于它作为矩阵的列的数量
    public final int glIndexSize;
    /// 每列的元素数量
    public final int componentCount;

    CGType(int componentCount, int alignment, int std140Alignment, @enumtype(VkFormat.class) int vkFormat, boolean isMat) {
        var columnCount = isMat ? componentCount : 1;
        this.byteSize = FLOAT_SIZE * columnCount * componentCount;
        this.alignment = alignment;
        this.std140Alignment = std140Alignment;
        this.vkFormat = vkFormat;
        this.glIndexSize = columnCount;
        this.componentCount = componentCount;
    }
}
