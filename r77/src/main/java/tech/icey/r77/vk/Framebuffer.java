package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class Framebuffer implements ManualDispose {
    public Framebuffer(Device device, int width, int height, LongBuffer pAttachments, long vkRenderPass) {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .pAttachments(pAttachments)
                    .width(width)
                    .height(height)
                    .layers(1)
                    .renderPass(vkRenderPass);

            LongBuffer framebufferBuffer = stack.mallocLong(1);
            int ret = vkCreateFramebuffer(device.vkDevice(), framebufferCreateInfo, null, framebufferBuffer);
            if (ret != VK_SUCCESS) {
                runtimeError("无法创建帧缓冲");
            }

            vkFramebuffer = framebufferBuffer.get(0);
        }
    }

    public Device device() {
        assert !isDisposed;
        return device;
    }

    public long vkFramebuffer() {
        assert !isDisposed;
        return vkFramebuffer;
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
        vkDestroyFramebuffer(device.vkDevice(), vkFramebuffer, null);
    }

    private final Device device;
    private final long vkFramebuffer;
    private volatile boolean isDisposed = false;
}
