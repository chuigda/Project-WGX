package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.util.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.datatype.GLFWimage;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.annotation.pointer;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.IntBuffer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public final class VulkanWindow implements AutoCloseable {
    private final GLFW glfw;
    private final @NotNull GLFWwindow rawWindow;
    private boolean framebufferResized = false;

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

        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT
        );

        try {
            MethodHandle handle = MethodHandles.lookup().findVirtual(
                    VulkanWindow.class,
                    "framebufferSizeCallback",
                    descriptor.toMethodType()
            ).bindTo(this);
            MemorySegment segment = Linker.nativeLinker().upcallStub(handle, descriptor, Arena.global());
            glfw.glfwSetFramebufferSizeCallback(rawWindow, segment);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("找不到回调函数 VulkanWindow::framebufferSizeCallback", e);
        }

        try (Arena arena = Arena.ofConfined()) {
            BufferedImage image = ImageUtil.loadImageFromResource("/resources/icon/icon-v2.png");
            GLFWimage glfwImage = ImageUtil.image2glfw(arena, image);
            glfw.glfwSetWindowIcon(rawWindow, 1, glfwImage);
        } catch (IOException e) {
            logger.warning("无法加载窗口图标: " + e.getMessage());
        }
    }

    public void mainLoop(VulkanRenderEngine renderer) throws RenderException {
        renderer.initEngine(glfw, rawWindow);
        while (glfw.glfwWindowShouldClose(rawWindow) != GLFWConstants.GLFW_TRUE) {
            if (framebufferResized) {
                framebufferResized = false;
                int width, height;
                try (Arena arena = Arena.ofConfined()) {
                    IntBuffer pWidthHeight = IntBuffer.allocate(arena, 2);
                    IntBuffer pWidth = pWidthHeight;
                    IntBuffer pHeight = pWidthHeight.offset(1);
                    glfw.glfwGetFramebufferSize(rawWindow, pWidth, pHeight);
                    width = pWidth.read();
                    height = pHeight.read();
                }
                renderer.resizeEngine(width, height);
            }

            renderer.renderFrameEngine();
            glfw.glfwPollEvents();
        }
        renderer.closeEngine();
    }

    @Override
    public void close() {
        logger.info("关闭 GLFW 窗口");
        glfw.glfwDestroyWindow(rawWindow);
    }

    private void framebufferSizeCallback(@pointer(comment="GLFWwindow*") MemorySegment window, int width, int height) {
        framebufferResized = true;
    }

    private static final Logger logger = Logger.getLogger(VulkanWindow.class.getName());
}
