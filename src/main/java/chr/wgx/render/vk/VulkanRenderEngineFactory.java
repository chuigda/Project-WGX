package chr.wgx.render.vk;

import chr.wgx.render.IRenderEngineFactory;
import chr.wgx.render.RenderException;
import chr.wgx.render.RenderWindow;
import org.jetbrains.annotations.Nullable;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.buffer.ByteBuffer;

import java.lang.foreign.Arena;

public final class VulkanRenderEngineFactory implements IRenderEngineFactory {
    VulkanRenderEngineFactory() {}

    @Override
    public RenderWindow createRenderWindow(GLFW glfw, String title, int width, int height) throws RenderException {
        if (glfw.glfwVulkanSupported() != GLFWConstants.GLFW_TRUE) {
            throw new RenderException("GLFW 报告不支持 Vulkan");
        }

        glfw.glfwWindowHint(GLFWConstants.GLFW_CLIENT_API, GLFWConstants.GLFW_NO_API);
        try (Arena arena = Arena.ofConfined()) {
            ByteBuffer titleBuffer = ByteBuffer.allocateString(arena, title);
            @Nullable GLFWwindow rawWindow = glfw.glfwCreateWindow(width, height, titleBuffer, null, null);
            if (rawWindow == null) {
                throw new RenderException("无法创建 GLFW 窗口");
            }

            VulkanRenderEngine engine = new VulkanRenderEngine(glfw, rawWindow);
            return new RenderWindow(glfw, rawWindow, engine);
        }
    }
}
