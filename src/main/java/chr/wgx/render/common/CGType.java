package chr.wgx.render.common;

import org.objectweb.asm.tree.analysis.Value;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.enumtype.VkFormat;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

public enum CGType {
    Float (ValueLayout.JAVA_FLOAT, 1, 4,  VkFormat.VK_FORMAT_R32_SFLOAT,          false),
    Int   (ValueLayout.JAVA_INT,   1, 4,  VkFormat.VK_FORMAT_R32_SINT,            false),

    Vec2 (MemoryLayout.sequenceLayout(2, ValueLayout.JAVA_FLOAT), 2, 8,  VkFormat.VK_FORMAT_R32G32_SFLOAT,       false),
    Vec3 (MemoryLayout.sequenceLayout(3, ValueLayout.JAVA_FLOAT), 3, 16, VkFormat.VK_FORMAT_R32G32B32_SFLOAT,    false),
    Vec4 (MemoryLayout.sequenceLayout(4, ValueLayout.JAVA_FLOAT), 4, 16, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, false),

    Mat2 (MemoryLayout.sequenceLayout(2 * 2, ValueLayout.JAVA_FLOAT), 2, 16, VkFormat.VK_FORMAT_R32G32_SFLOAT, true),
    Mat3 (MemoryLayout.sequenceLayout(3 * 3, ValueLayout.JAVA_FLOAT), 3, 16, VkFormat.VK_FORMAT_R32G32B32_SFLOAT, true),
    Mat4 (MemoryLayout.sequenceLayout(4 * 4, ValueLayout.JAVA_FLOAT), 4, 16, VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT, true);

    private static final int FLOAT_SIZE = 4;

    /// 对应的 CPU memory layout
    public final MemoryLayout cpuLayout;
    /// 数据类型的分量数量
    public final int componentCount;
    /// 数据类型在 std140 布局中的对齐
    public final int std140Alignment;
    /// 对应的 Vulkan format
    public final @enumtype(VkFormat.class) int vkFormat;
    /// 是否是矩阵类型
    public final boolean isMat;

    public final int byteSize;
    public final int cpuAlignment;

    CGType(
            MemoryLayout cpuLayout,
            int componentCount,
            int std140Alignment,
            @enumtype(VkFormat.class) int vkFormat,
            boolean isMat
    ) {
        this.cpuLayout = cpuLayout;
        this.componentCount = componentCount;
        this.std140Alignment = std140Alignment;
        this.vkFormat = vkFormat;
        this.isMat = isMat;

        this.byteSize = (int) cpuLayout.byteSize();
        this.cpuAlignment = (int) cpuLayout.byteAlignment();

        assert cpuAlignment <= std140Alignment;
    }
}
