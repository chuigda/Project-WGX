package chr.wgx.render;

import chr.wgx.util.ImageUtil;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.GLFWConstants;
import tech.icey.glfw.datatype.GLFWimage;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.annotation.pointer;
import tech.icey.panama.buffer.IntBuffer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public final class RenderWindow implements AutoCloseable {
    public final GLFW glfw;
    public final GLFWwindow rawWindow;
    public final RenderEngine renderEngine;
    public boolean framebufferResized = false;

    public RenderWindow(GLFW glfw, GLFWwindow rawWindow, RenderEngine renderEngine) {
        this.glfw = glfw;
        this.rawWindow = rawWindow;
        this.renderEngine = renderEngine;

        FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT
        );

        try {
            MethodHandle handle = MethodHandles.lookup().findVirtual(
                    RenderWindow.class,
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

    public void mainLoop() throws RenderException {
        while (glfw.glfwWindowShouldClose(rawWindow) != GLFWConstants.GLFW_TRUE) {
            glfw.glfwPollEvents();
            if (framebufferResized) {
                framebufferResized = false;
                int width, height;
                try (Arena arena = Arena.ofConfined()) {
                    IntBuffer pWidthHeight = IntBuffer.allocate(arena, 2);
                    IntBuffer pWidth = pWidthHeight.offset(0);
                    IntBuffer pHeight = pWidthHeight.offset(1);
                    glfw.glfwGetFramebufferSize(rawWindow, pWidth, pHeight);
                    width = pWidth.read();
                    height = pHeight.read();
                }

                renderEngine.resizeEngine(width, height);
            }

            renderEngine.renderFrameEngine();
        }

        renderEngine.closeEngine();
    }

    @Override
    public void close() {
        glfw.glfwDestroyWindow(rawWindow);
    }

    @SuppressWarnings("unused")
    private void framebufferSizeCallback(@pointer(comment="GLFWwindow*") MemorySegment window, int width, int height) {
        framebufferResized = true;
    }

    private static final Logger logger = Logger.getLogger(RenderWindow.class.getName());
}
