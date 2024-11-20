package chr.wgx.render.gles2;

import org.jetbrains.annotations.NotNull;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.ByteBuffer;

import java.lang.foreign.Arena;
import java.util.logging.Logger;

public final class GLES2Window implements AutoCloseable {
    private final GLFW glfw;
    private final @NotNull GLFWwindow rawWindow;

    public GLES2Window(GLFW glfw, String title, int width, int height) {
        glfw.glfwWindowHint(GLFWConstants.GLFW_CLIENT_API, GLFWConstants.GLFW_OPENGL_ES_API);
        glfw.glfwWindowHint(GLFWConstants.GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfw.glfwWindowHint(GLFWConstants.GLFW_CONTEXT_VERSION_MINOR, 0);
        glfw.glfwWindowHint(GLFWConstants.GLFW_OPENGL_PROFILE, GLFWConstants.GLFW_OPENGL_ES_API);

        try (Arena arena = Arena.ofConfined()) {
            ByteBuffer titleBuffer = ByteBuffer.allocateString(arena, title);
            GLFWwindow window = glfw.glfwCreateWindow(width, height, titleBuffer, null, null);
            if (window == null) {
                throw new RuntimeException("无法创建 GLFW 窗口");
            }
            this.glfw = glfw;
            this.rawWindow = window;
        }
    }

    @Override
    public void close() throws Exception {
        logger.info("关闭 GLFW 窗口");
        glfw.glfwDestroyWindow(rawWindow);
    }

    private static final Logger logger = Logger.getLogger(GLES2Window.class.getName());
}
