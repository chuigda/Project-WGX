package tech.icey.r77.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkQueue;

import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;

public abstract sealed class Queue permits GraphicsQueue, PresentQueue {
    public Queue(Device device, int queueFamilyIndex, int queueIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pQueue = stack.mallocPointer(1);
            vkGetDeviceQueue(device.vkDevice(), queueFamilyIndex, queueIndex, pQueue);
            long queue = pQueue.get(0);

            vkQueue = new VkQueue(queue, device.vkDevice());
            this.queueFamilyIndex = queueFamilyIndex;
        }
    }

    public final void waitIdle() {
        vkQueueWaitIdle(vkQueue);
    }

    public final VkQueue vkQueue;
    public final int queueFamilyIndex;
}
