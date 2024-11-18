package chr.wgx.render;

import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.ByteBuffer;

import java.lang.foreign.Arena;
import java.util.logging.Logger;

public final class RenderWindow implements AutoCloseable {
    private final GLFW glfw;
    private final GLFWwindow rawWindow;

    private RenderWindow(GLFW glfw, GLFWwindow rawWindow) {
        this.glfw = glfw;
        this.rawWindow = rawWindow;
    }

    public GLFWwindow rawWindow() {
        return rawWindow;
    }

    public void mainLoop() {
        while (glfw.glfwWindowShouldClose(rawWindow) != GLFWConstants.GLFW_TRUE) {
            glfw.glfwPollEvents();
        }
    }

    public static RenderWindow createVulkanWindow(GLFW glfw, String title, int width, int height) {
        if (glfw.glfwVulkanSupported() != GLFWConstants.GLFW_TRUE) {
            throw new RuntimeException("GLFW 报告不支持 Vulkan");
        }

        glfw.glfwWindowHint(GLFWConstants.GLFW_CLIENT_API, GLFWConstants.GLFW_NO_API);
        try (Arena arena = Arena.ofConfined()) {
            ByteBuffer titleBuffer = ByteBuffer.allocateString(arena, title);
            GLFWwindow window = glfw.glfwCreateWindow(width, height,titleBuffer, null, null);
            if (window == null) {
                throw new RuntimeException("无法创建 GLFW 窗口");
            }
            return new RenderWindow(glfw, window);
        }
    }

    public static RenderWindow createGLES2Window(GLFW glfw, String title, int width, int height) {
        glfw.glfwWindowHint(GLFWConstants.GLFW_CLIENT_API, GLFWConstants.GLFW_OPENGL_ES_API);
        glfw.glfwWindowHint(GLFWConstants.GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfw.glfwWindowHint(GLFWConstants.GLFW_CONTEXT_VERSION_MINOR, 0);
        glfw.glfwWindowHint(GLFWConstants.GLFW_OPENGL_PROFILE, GLFWConstants.GLFW_OPENGL_ANY_PROFILE);

        try (Arena arena = Arena.ofConfined()) {
            ByteBuffer titleBuffer = ByteBuffer.allocateString(arena, title);
            GLFWwindow window = glfw.glfwCreateWindow(width, height, titleBuffer, null, null);
            if (window == null) {
                throw new RuntimeException("无法创建 GLFW 窗口");
            }
            return new RenderWindow(glfw, window);
        }
    }

    @Override
    public void close() {
        logger.info("关闭 GLFW 窗口");
        glfw.glfwDestroyWindow(rawWindow);
    }

    private static final Logger logger = Logger.getLogger(RenderWindow.class.getName());
}
