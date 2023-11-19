package tech.icey.r77.vk;

import tech.icey.r77.Init;
import tech.icey.util.NotNull;
import tech.icey.util.Pair;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class VkWindow implements AutoCloseable {
    public VkWindow(String title, int width, int height) {
        if (!Init.isInitialised()) {
            throw new RuntimeException("尚未初始化 GLFW");
        }
        
        if (!Init.isVulkanSupported()) {
            throw new RuntimeException("你所在的平台不支持 Vulkan");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);

        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if (windowHandle == NULL) {
            throw new RuntimeException("创建 GLFW 窗口失败");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> resizeEvent(w, h));
        glfwShowWindow(windowHandle);
    }

    public final boolean poll() {
        glfwPollEvents();
        return !glfwWindowShouldClose(windowHandle);
    }

    public final void closeWindow() {
        if (windowHandle != NULL) {
            glfwDestroyWindow(windowHandle);
            windowHandle = NULL;
        }
    }

    public final int getWidth() {
        int[] width = new int[1];
        glfwGetWindowSize(windowHandle, width, null);
        return width[0];
    }

    public final int getHeight() {
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, null, height);
        return height[0];
    }

    public final @NotNull Pair<Integer, Integer> getSize() {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        return new Pair<>(width[0], height[0]);
    }

    public final void setWidth(int width) {
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, null, height);
        glfwSetWindowSize(windowHandle, width, height[0]);
    }

    public final void setHeight(int height) {
        int[] width = new int[1];
        glfwGetWindowSize(windowHandle, width, null);
        glfwSetWindowSize(windowHandle, width[0], height);
    }

    public final void setSize(int width, int height) {
        glfwSetWindowSize(windowHandle, width, height);
    }

    @Override
    public void close() {
        closeWindow();
    }

    private void resizeEvent(int w, int h) {
    }

    private long windowHandle;
}
