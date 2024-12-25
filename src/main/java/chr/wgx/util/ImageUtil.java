package chr.wgx.util;

import tech.icey.glfw.datatype.GLFWimage;
import tech.icey.panama.buffer.ByteBuffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;

public final class ImageUtil {
    public static BufferedImage loadImageFromResource(String path) throws IOException {
        try (InputStream is = ImageUtil.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found: " + path);
            }

            return javax.imageio.ImageIO.read(is);
        }
    }

    public static BufferedImage loadImageFromFileSystem(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static void writeImageToBuffer(BufferedImage image, ByteBuffer buffer) {
        int width = image.getWidth();
        int height = image.getHeight();

        assert buffer.size() == (long) width * height * 4;

        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            byte r = (byte) ((pixel >> 16) & 0xFF);
            byte g = (byte) ((pixel >> 8) & 0xFF);
            byte b = (byte) (pixel & 0xFF);
            byte a = (byte) ((pixel >> 24) & 0xFF);

            buffer.write((long) i * 4, r);
            buffer.write((long) i * 4 + 1, g);
            buffer.write((long) i * 4 + 2, b);
            buffer.write((long) i * 4 + 3, a);
        }
    }

    public static GLFWimage image2glfw(Arena arena, BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        ByteBuffer buffer = ByteBuffer.allocate(arena, (long) width * height * 4);
        writeImageToBuffer(image, buffer);

        GLFWimage glfwImage = GLFWimage.allocate(arena);
        glfwImage.width(width);
        glfwImage.height(height);
        glfwImage.pixels(buffer);
        return glfwImage;
    }
}
