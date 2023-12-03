package tech.icey.r77.gl;

import org.lwjgl.opengl.GL;
import tech.icey.r77.Init;
import tech.icey.util.NotNull;
import tech.icey.util.Pair;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static tech.icey.util.RuntimeError.*;

public abstract class GLWindow implements AutoCloseable {
    public GLWindow(@NotNull String title, int width, int height, boolean vsync) {
        if (!Init.isInitialised()) {
            unreachable("尚未初始化 GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);

        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if (windowHandle == NULL) {
            runtimeError("你所在的平台不支持 OpenGL");
        }

        if (vsync) {
            glfwMakeContextCurrent(windowHandle);
            glfwSwapInterval(1);
        }

        glfwSetWindowSizeCallback(
                windowHandle,
                (window, width1, height1) -> {
                    makeCurrent();
                    resizeGL(width1, height1);
                    doneCurrent();
                }
        );

        glfwSetWindowCloseCallback(windowHandle, window -> glfwSetWindowShouldClose(window, false));

        makeCurrent();
        GL.createCapabilities();
        initialiseGL();
        resizeGL(width, height);
        doneCurrent();

        glfwShowWindow(windowHandle);
    }

    public GLWindow(@NotNull String title, int width, int height) {
        this(title, width, height, true);
    }

    public abstract void initialiseGL();
    public abstract void paintGL();
    public abstract void resizeGL(int width, int height);

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

    public final void makeCurrent() {
        glfwMakeContextCurrent(windowHandle);
    }

    public final void doneCurrent() {
        glfwMakeContextCurrent(0);
    }

    public final boolean poll() {
        makeCurrent();
        paintGL();
        glfwSwapBuffers(windowHandle);
        doneCurrent();

        glfwPollEvents();
        return !glfwWindowShouldClose(windowHandle);
    }

    @Override
    public void close() {
        closeWindow();
    }

    private long windowHandle;
}
