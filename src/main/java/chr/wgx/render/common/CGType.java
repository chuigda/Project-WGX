package chr.wgx.render.common;

public enum CGType {
    Float (4,         4,  4),
    Vec2  (4 * 2,     4,  8),
    Vec3  (4 * 3,     4, 16),
    Vec4  (4 * 4,     4, 16),
    Mat2  (2 * 2 * 4, 4, 16),
    Mat3  (3 * 3 * 4, 4, 16),
    Mat4  (4 * 4 * 4, 4, 16);

    /// 数据类型在 CPU 中存储时消耗的字节数
    public final int byteSize;
    /// 数据类型在 C ABI 中的对齐
    public final int alignment;
    /// 数据类型在 std140 布局中的对齐
    public final int std140Alignment;

    CGType(int byteSize, int alignment, int std140Alignment) {
        this.byteSize = byteSize;
        this.alignment = alignment;
        this.std140Alignment = std140Alignment;
    }
}
