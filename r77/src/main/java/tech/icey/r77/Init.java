package tech.icey.r77;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;

public class Init {
    private static boolean initialised = false;
    private static boolean vulkanSupported = false;

    public synchronized static void initialise() {
        if (initialised) {
            throw new RuntimeException("GLFW 已经被初始化过了!");
        }

        if (!glfwInit()) {
            throw new RuntimeException("GLFW 初始化失败!");
        }

        vulkanSupported = glfwVulkanSupported();
        initialised = true;
    }

    public synchronized static boolean isInitialised() {
        return initialised;
    }

    public synchronized static boolean isVulkanSupported() {
        return vulkanSupported;
    }
}
