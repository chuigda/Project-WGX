package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.ByteBuffer;

import java.lang.foreign.Arena;
import java.util.logging.Logger;

public final class VulkanWindow implements AutoCloseable {
    private final GLFW glfw;
    private final @NotNull GLFWwindow rawWindow;

    public VulkanWindow(GLFW glfw, String title, int width, int height) throws RenderException {
        if (glfw.glfwVulkanSupported() != GLFWConstants.GLFW_TRUE) {
            throw new RenderException("GLFW 报告不支持 Vulkan");
        }

        glfw.glfwWindowHint(GLFWConstants.GLFW_CLIENT_API, GLFWConstants.GLFW_NO_API);
        try (Arena arena = Arena.ofConfined()) {
            ByteBuffer titleBuffer = ByteBuffer.allocateString(arena, title);
            @Nullable GLFWwindow window = glfw.glfwCreateWindow(width, height, titleBuffer, null, null);
            if (window == null) {
                throw new RenderException("无法创建 GLFW 窗口");
            }
            this.glfw = glfw;
            this.rawWindow = window;
        }
    }

    @Override
    public void close() {
        logger.info("关闭 GLFW 窗口");
        glfw.glfwDestroyWindow(rawWindow);
    }

    private static final Logger logger = Logger.getLogger(VulkanWindow.class.getName());
}
