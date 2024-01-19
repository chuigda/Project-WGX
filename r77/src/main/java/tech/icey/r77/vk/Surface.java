package tech.icey.r77.vk;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

public final class Surface implements ManualDispose {
	public Surface(PhysicalDevice physicalDevice, long windowHandle) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer surfaceBuf = stack.mallocLong(1);
            GLFWVulkan.glfwCreateWindowSurface(
            		physicalDevice.vkPhysicalDevice.getInstance(),
            		windowHandle,
                    null,
                    surfaceBuf
            );
            long vkSurface = surfaceBuf.get(0);

            this.physicalDevice = physicalDevice;
            this.vkSurface = vkSurface;
        }
	}

    public PhysicalDevice physicalDevice() {
        assert !isDisposed;
        return physicalDevice;
    }

    public long vkSurface() {
        assert !isDisposed;
        return vkSurface;
    }

    @Override
    public boolean isManuallyDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            KHRSurface.vkDestroySurfaceKHR(physicalDevice.vkPhysicalDevice.getInstance(), vkSurface, null);
            isDisposed = true;
        }
    }

    private final PhysicalDevice physicalDevice;
    private final long vkSurface;
    private volatile boolean isDisposed = false;
}
