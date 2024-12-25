package chr.wgx.util;

public final class ColorUtil {
    // HSV to RGB, and S and V are both 1.0
    public static float[] hueToRGB(int hue) {
        float h = (float) hue / 360.0f;
        float r = Math.abs(h * 6.0f - 3.0f) - 1.0f;
        float g = 2.0f - Math.abs(h * 6.0f - 2.0f);
        float b = 2.0f - Math.abs(h * 6.0f - 4.0f);
        return new float[]{r, g, b};
    }
}
