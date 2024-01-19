package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class PresentQueue extends Queue {
    public PresentQueue(Device device, Surface surface, int queueIndex) {
        super(device, getPresentQueueFamilyIndex(device, surface), queueIndex);
    }

    private static int getPresentQueueFamilyIndex(Device device, Surface surface) {
        int index = -1;
        PhysicalDevice physicalDevice = device.physicalDevice();
        List<VkQueueFamilyProperties> queueFamilyProperties =
                physicalDevice.physicalDeviceProperties.graphicsQueueFamilies;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer canPresentBuf = stack.mallocInt(1);
            VkPhysicalDevice vkPhysicalDevice = device.physicalDevice().vkPhysicalDevice;
            for (int i = 0; i < queueFamilyProperties.size(); i++) {
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(
                        vkPhysicalDevice,
                        i,
                        surface.vkSurface(),
                        canPresentBuf
                );

                if (canPresentBuf.get(0) == VK_TRUE) {
                    index = i;
                    break;
                }
            }
        }

        if (index < 0) {
            runtimeError("Failed to get graphics Queue family index");
        }
        return index;
    }
}
