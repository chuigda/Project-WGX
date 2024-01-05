package tech.icey.r77.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import tech.icey.util.ManualDispose;

import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK13.VK_API_VERSION_1_3;
import static tech.icey.util.RuntimeError.runtimeError;

public final class MemoryAllocator implements ManualDispose {
    public MemoryAllocator(Instance instance, Device device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaAllocatorCreateInfo vmaAllocatorCreateInfo = VmaAllocatorCreateInfo.calloc(stack);
            vmaAllocatorCreateInfo.vulkanApiVersion(VK_API_VERSION_1_3);
            vmaAllocatorCreateInfo.instance(instance.vkInstance);
            vmaAllocatorCreateInfo.physicalDevice(device.physicalDevice.vkPhysicalDevice);
            vmaAllocatorCreateInfo.device(device.vkDevice);
            VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.calloc(stack);
            vmaVulkanFunctions.set(instance.vkInstance, device.vkDevice);
            vmaAllocatorCreateInfo.pVulkanFunctions(vmaVulkanFunctions);

            PointerBuffer pAllocator = stack.mallocPointer(1);
            if (Vma.vmaCreateAllocator(vmaAllocatorCreateInfo, pAllocator) != VK_SUCCESS) {
                runtimeError("创建内存分配器失败");
            }

            vma = pAllocator.get(0);
        }
    }

    public final long vma;

    @Override
    public boolean isManuallyDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            Vma.vmaDestroyAllocator(vma);
            isDisposed = true;
        }
    }

    private volatile boolean isDisposed = false;
}
