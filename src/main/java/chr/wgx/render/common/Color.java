package chr.wgx.render.common;

import club.doki7.ffm.buffer.FloatBuffer;
import club.doki7.vulkan.datatype.VkClearColorValue;

public final class Color {
    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public void writeTo(VkClearColorValue vkColor) {
        writeTo(vkColor.float32());
    }

    public void writeTo(FloatBuffer buffer) {
        assert buffer.size() >= 4;

        buffer.write(0, r);
        buffer.write(1, g);
        buffer.write(2, b);
        buffer.write(3, a);
    }
}
