package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class CommandPool implements ManualDispose {
    public CommandPool(Device device, int queueFamilyIndex) {
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo commandPoolCreateInfo = VkCommandPoolCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(queueFamilyIndex);

            LongBuffer commandPoolBuffer = stack.mallocLong(1);
            int ret = vkCreateCommandPool(device.vkDevice, commandPoolCreateInfo, null, commandPoolBuffer);
            if (ret != VK_SUCCESS) {
                runtimeError("无法创建指令池");
            }

            vkCommandPool = commandPoolBuffer.get(0);
        }
    }

    public final Device device;
    public final long vkCommandPool;

    @Override
    public boolean isManuallyDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (isDisposed) {
            return;
        }

        isDisposed = true;
        vkDestroyCommandPool(device.vkDevice, vkCommandPool, null);
    }

    private boolean isDisposed = false;
}
