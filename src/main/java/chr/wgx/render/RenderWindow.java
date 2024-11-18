package chr.wgx.render;

import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.ByteBuffer;

import java.lang.foreign.Arena;

public final class RenderWindow {
    private final GLFW glfw;
    private final GLFWwindow rawWindow;

    private RenderWindow(GLFW glfw, GLFWwindow rawWindow) {
        this.glfw = glfw;
        this.rawWindow = rawWindow;
    }

    public GLFWwindow rawWindow() {
        return rawWindow;
    }

    public static RenderWindow createVulkanWindow(GLFW glfw, String title, int width, int height) {
        if (glfw.glfwVulkanSupported() != GLFWConstants.GLFW_TRUE) {
            throw new RuntimeException("GLFW 报告不支持 Vulkan");
        }

        glfw.glfwWindowHint(GLFWConstants.GLFW_CLIENT_API, GLFWConstants.GLFW_NO_API);
        try (Arena arena = Arena.ofConfined()) {
            ByteBuffer titleBuffer = ByteBuffer.allocateString(arena, title);
            GLFWwindow window = glfw.glfwCreateWindow(width, height,titleBuffer, null, null);
            return new RenderWindow(glfw, window);
        }
    }
}
