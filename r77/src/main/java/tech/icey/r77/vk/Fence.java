package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class Fence implements ManualDispose {
    public Fence(Device device, boolean signaled) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO)
                    .flags(signaled ? VK_FENCE_CREATE_SIGNALED_BIT : 0);

            LongBuffer fenceBuf = stack.mallocLong(1);
            int ret = vkCreateFence(device.vkDevice(), fenceCreateInfo, null, fenceBuf);
            if (ret != VK_SUCCESS) {
                runtimeError("无法创建 Vulkan 栅栏对象");
            }

            vkFence = fenceBuf.get(0);
        }
    }

    public Device device() {
        assert !isDisposed;
        return device;
    }

    public long vkFence() {
        assert !isDisposed;
        return vkFence;
    }

    public void fenceWait() {
        assert !isDisposed;
        vkWaitForFences(device.vkDevice(), vkFence, true, Long.MAX_VALUE);
    }

    public void reset() {
        assert !isDisposed;
        vkResetFences(device.vkDevice(), vkFence);
    }

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
        vkDestroyFence(device.vkDevice(), vkFence, null);
    }

    private final Device device;
    private final long vkFence;
    private volatile boolean isDisposed = false;
}
