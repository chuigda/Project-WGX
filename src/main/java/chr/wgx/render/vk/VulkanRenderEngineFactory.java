package chr.wgx.render.vk;

import chr.wgx.render.IRenderEngineFactory;
import chr.wgx.render.RenderException;
import chr.wgx.render.RenderWindow;
import org.jetbrains.annotations.Nullable;
import club.doki7.glfw.GLFW;
import club.doki7.glfw.GLFWConstants;
import club.doki7.glfw.handle.GLFWwindow;
import club.doki7.ffm.ptr.BytePtr;

import java.lang.foreign.Arena;

public final class VulkanRenderEngineFactory implements IRenderEngineFactory {
    VulkanRenderEngineFactory() {}

    @Override
    public RenderWindow createRenderWindow(GLFW glfw, String title, int width, int height) throws RenderException {
        if (glfw.vulkanSupported() != GLFWConstants.TRUE) {
            throw new RenderException("GLFW 报告不支持 Vulkan");
        }

        glfw.windowHint(GLFWConstants.CLIENT_API, GLFWConstants.NO_API);
        try (Arena arena = Arena.ofConfined()) {
            BytePtr titleBuffer = BytePtr.allocateString(arena, title);
            @Nullable GLFWwindow rawWindow = glfw.createWindow(width, height, titleBuffer, null, null);
            if (rawWindow == null) {
                throw new RenderException("无法创建 GLFW 窗口");
            }

            VulkanRenderEngine engine = new VulkanRenderEngine(glfw, rawWindow);
            return new RenderWindow(glfw, rawWindow, engine);
        }
    }
}
