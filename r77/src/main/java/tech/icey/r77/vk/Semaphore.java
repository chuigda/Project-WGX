package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class Semaphore implements ManualDispose {
    public Semaphore(Device device) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSemaphoreCreateInfo semaphoreCreateInfo = VkSemaphoreCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            LongBuffer semaphoreBuf = stack.mallocLong(1);
            int ret = vkCreateSemaphore(device.vkDevice, semaphoreCreateInfo, null, semaphoreBuf);
            if (ret != 0) {
                runtimeError("无法创建 Vulkan 信号量对象");
            }

            vkSemaphore = semaphoreBuf.get(0);
        }
    }

    public final Device device;
    public final long vkSemaphore;

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
    }

    private boolean isDisposed = false;
}
