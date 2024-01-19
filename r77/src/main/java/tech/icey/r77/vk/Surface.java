package tech.icey.r77.vk;

import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static tech.icey.util.RuntimeError.runtimeError;

public final class Surface implements ManualDispose {
	public Surface(PhysicalDevice physicalDevice, long windowHandle) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer surfaceBuf = stack.mallocLong(1);
            int ret = GLFWVulkan.glfwCreateWindowSurface(
            		physicalDevice.vkPhysicalDevice.getInstance(),
            		windowHandle,
                    null,
                    surfaceBuf
            );
            if (ret != VK_SUCCESS) {
                runtimeError("无法从 GLFW 窗口创建 Vulkan 表面：%d", ret);
            }
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
