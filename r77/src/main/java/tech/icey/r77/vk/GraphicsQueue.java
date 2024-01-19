package tech.icey.r77.vk;

import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static tech.icey.util.RuntimeError.runtimeError;

public final class GraphicsQueue extends Queue {
    public GraphicsQueue(Device device, int queueIndex) {
        super(device, getGraphicsQueueFamilyIndex(device), queueIndex);
    }

    private static int getGraphicsQueueFamilyIndex(Device device) {
        int index = -1;
        PhysicalDevice physicalDevice = device.physicalDevice();
        List<VkQueueFamilyProperties> queueFamilyProperties =
                physicalDevice.physicalDeviceProperties.graphicsQueueFamilies;
        for (int i = 0; i < queueFamilyProperties.size(); i++) {
            VkQueueFamilyProperties props = queueFamilyProperties.get(i);
            boolean graphicsQueue = (props.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0;
            if (graphicsQueue) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            runtimeError("Failed to get graphics Queue family index");
        }
        return index;
    }
}
