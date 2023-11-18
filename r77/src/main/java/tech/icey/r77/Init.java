package tech.icey.r77;

import static org.lwjgl.glfw.GLFW.*;

public class Init {
    private static boolean initialised = false;

    public synchronized static void initialise() {
        if (initialised) {
            throw new RuntimeException("GLFW has already been initialised!");
        }

        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialise GLFW!");
        }
        initialised = true;
    }
}
