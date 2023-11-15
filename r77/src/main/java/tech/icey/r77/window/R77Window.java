package tech.icey.r77.window;

import tech.icey.util.Function3;
import tech.icey.util.NotNull;
import tech.icey.util.Pair;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class R77Window implements AutoCloseable {
    public R77Window(@NotNull String title, int width, int height, boolean vsync) {
        glfwDefaultWindowHints();
        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create GLFW window!");
        }

        if (vsync) {
            glfwMakeContextCurrent(windowHandle);
            glfwSwapInterval(1);
        }
        glfwShowWindow(windowHandle);
    }

    public R77Window(@NotNull String title, int width, int height) {
        this(title, width, height, true);
    }

    public void setWindowSizeCallback(@NotNull Function3<R77Window, Integer, Integer, Void> callback) {
        glfwSetWindowSizeCallback(
                windowHandle,
                (window, width, height) -> callback.apply(this, width, height)
        );
    }

    public boolean requestedToClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    public void beforePaint() {
        makeCurrent();
    }

    public void afterPaint() {
        swapBuffers();
        doneCurrent();

        pollEvents();
    }

    public void swapBuffers() {
        glfwSwapBuffers(windowHandle);
    }

    public void makeCurrent() {
        glfwMakeContextCurrent(windowHandle);
    }

    public void doneCurrent() {
        glfwMakeContextCurrent(0);
    }

    public int getWidth() {
        int[] width = new int[1];
        glfwGetWindowSize(windowHandle, width, null);
        return width[0];
    }

    public int getHeight() {
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, null, height);
        return height[0];
    }

    public @NotNull Pair<Integer, Integer> getSize() {
        int[] width = new int[1];
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, width, height);
        return new Pair<>(width[0], height[0]);
    }

    public void setWidth(int width) {
        int[] height = new int[1];
        glfwGetWindowSize(windowHandle, null, height);
        glfwSetWindowSize(windowHandle, width, height[0]);
    }

    public void setHeight(int height) {
        int[] width = new int[1];
        glfwGetWindowSize(windowHandle, width, null);
        glfwSetWindowSize(windowHandle, width[0], height);
    }

    public void setSize(int width, int height) {
        glfwSetWindowSize(windowHandle, width, height);
    }

    public static void pollEvents() {
        glfwPollEvents();
    }

    @Override
    public void close() throws Exception {
        glfwDestroyWindow(windowHandle);
    }

    private final long windowHandle;
}
