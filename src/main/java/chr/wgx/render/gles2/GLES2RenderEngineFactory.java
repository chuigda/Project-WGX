package chr.wgx.render.gles2;

import chr.wgx.config.Config;
import chr.wgx.render.IRenderEngineFactory;
import chr.wgx.render.RenderException;
import chr.wgx.render.RenderWindow;
import org.jetbrains.annotations.Nullable;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.ByteBuffer;

import java.lang.foreign.Arena;

public final class GLES2RenderEngineFactory implements IRenderEngineFactory {
    GLES2RenderEngineFactory() {}

    @Override
    public RenderWindow createRenderWindow(GLFW glfw, String title, int width, int height) throws RenderException {
        Config config = Config.config();

        glfw.glfwWindowHint(GLFWConstants.GLFW_CLIENT_API, GLFWConstants.GLFW_OPENGL_ES_API);
        glfw.glfwWindowHint(GLFWConstants.GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfw.glfwWindowHint(GLFWConstants.GLFW_CONTEXT_VERSION_MINOR, 0);
        glfw.glfwWindowHint(GLFWConstants.GLFW_OPENGL_PROFILE, GLFWConstants.GLFW_OPENGL_ES_API);
        if (config.gles2Config.debug) {
            glfw.glfwWindowHint(GLFWConstants.GLFW_OPENGL_DEBUG_CONTEXT, GLFWConstants.GLFW_TRUE);
        }

        try (Arena arena = Arena.ofConfined()) {
            ByteBuffer titleBuffer = ByteBuffer.allocateString(arena, title);
            @Nullable GLFWwindow window = glfw.glfwCreateWindow(width, height, titleBuffer, null, null);
            if (window == null) {
                throw new RenderException("无法创建 GLFW 窗口");
            }

            GLES2RenderEngine engine = new GLES2RenderEngine(glfw, window);
            return new RenderWindow(glfw, window, engine);
        }
    }
}
