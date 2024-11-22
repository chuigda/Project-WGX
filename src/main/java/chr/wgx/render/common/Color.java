package chr.wgx.render.common;

import tech.icey.panama.buffer.FloatBuffer;
import tech.icey.vk4j.datatype.VkClearColorValue;

public record Color(float r, float g, float b, float a) {
    public void writeTo(VkClearColorValue vkColor) {
        writeTo(vkColor.float32());
    }

    public void writeTo(FloatBuffer buffer) {
        assert buffer.size() == 4;
        buffer.write(0, r);
        buffer.write(1, g);
        buffer.write(2, b);
        buffer.write(3, a);
    }
}
