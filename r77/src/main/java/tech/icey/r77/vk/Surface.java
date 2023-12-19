package tech.icey.r77.vk;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;

import java.nio.LongBuffer;

public record Surface(PhysicalDevice physicalDevice, long vkSurface) {
	public static Surface create(PhysicalDevice physicalDevice, long windowHandle) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer surfaceBuf = stack.mallocLong(1);
            GLFWVulkan.glfwCreateWindowSurface(
            		physicalDevice.vkPhysicalDevice().getInstance(),
            		windowHandle,
                    null,
                    surfaceBuf
            );
            long vkSurface = surfaceBuf.get(0);
            return new Surface(physicalDevice, vkSurface);
        }
	}
}
