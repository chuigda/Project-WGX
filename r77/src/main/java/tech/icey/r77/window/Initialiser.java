package tech.icey.r77.window;

import static org.lwjgl.glfw.GLFW.*;

public class Initialiser {
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
