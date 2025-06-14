package chr.wgx.render.gles2;

import chr.wgx.config.Config;
import chr.wgx.render.IRenderEngineFactory;
import chr.wgx.render.RenderException;
import chr.wgx.render.RenderWindow;
import org.jetbrains.annotations.Nullable;
import club.doki7.glfw.GLFW;
import club.doki7.glfw.handle.GLFWwindow;
import club.doki7.ffm.ptr.BytePtr;

import java.lang.foreign.Arena;

public final class GLES2RenderEngineFactory implements IRenderEngineFactory {
    GLES2RenderEngineFactory() {}

    @Override
    public RenderWindow createRenderWindow(GLFW glfw, String title, int width, int height) throws RenderException {
        Config config = Config.config();

        glfw.windowHint(GLFW.CLIENT_API, GLFW.OPENGL_ES_API);
        glfw.windowHint(GLFW.CONTEXT_VERSION_MAJOR, 2);
        glfw.windowHint(GLFW.CONTEXT_VERSION_MINOR, 0);
        glfw.windowHint(GLFW.OPENGL_PROFILE, GLFW.OPENGL_ES_API);
        if (config.gles2Config.debug) {
            glfw.windowHint(GLFW.OPENGL_DEBUG_CONTEXT, GLFW.TRUE);
        }

        try (Arena arena = Arena.ofConfined()) {
            BytePtr titleBuffer = BytePtr.allocateString(arena, title);
            @Nullable GLFWwindow window = glfw.createWindow(width, height, titleBuffer, null, null);
            if (window == null) {
                throw new RenderException("无法创建 GLFW 窗口");
            }

            GLES2RenderEngine engine = new GLES2RenderEngine(glfw, window);
            return new RenderWindow(glfw, window, engine);
        }
    }
}
