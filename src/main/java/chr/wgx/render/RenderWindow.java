package chr.wgx.render;

import chr.wgx.util.ImageUtil;
import club.doki7.glfw.GLFW;
import club.doki7.glfw.GLFWConstants;
import club.doki7.glfw.datatype.GLFWimage;
import club.doki7.glfw.handle.GLFWwindow;
import club.doki7.ffm.annotation.Pointer;
import club.doki7.ffm.ptr.IntPtr;

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
            glfw.setFramebufferSizeCallback(rawWindow, segment);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("找不到回调函数 VulkanWindow::framebufferSizeCallback", e);
        }

        try (Arena arena = Arena.ofConfined()) {
            BufferedImage image = ImageUtil.loadImageFromResource("/resources/icon/icon-v2.png");
            GLFWimage glfwImage = ImageUtil.image2glfw(arena, image);
            glfw.setWindowIcon(rawWindow, 1, glfwImage);
        } catch (IOException e) {
            logger.warning("无法加载窗口图标: " + e.getMessage());
        }
    }

    public void mainLoop() throws RenderException {
        while (glfw.windowShouldClose(rawWindow) != GLFW.TRUE) {
            glfw.pollEvents();
            if (framebufferResized) {
                framebufferResized = false;
                int width, height;
                try (Arena arena = Arena.ofConfined()) {
                    IntPtr pWidthHeight = IntPtr.allocate(arena, 2);
                    IntPtr pWidth = pWidthHeight.offset(0);
                    IntPtr pHeight = pWidthHeight.offset(1);
                    glfw.getFramebufferSize(rawWindow, pWidth, pHeight);
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
        glfw.destroyWindow(rawWindow);
    }

    @SuppressWarnings("unused")
    private void framebufferSizeCallback(@Pointer(comment="GLFWwindow*") MemorySegment window, int width, int height) {
        framebufferResized = true;
    }

    private static final Logger logger = Logger.getLogger(RenderWindow.class.getName());
}
