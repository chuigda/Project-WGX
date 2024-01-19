package tech.icey.r77.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import tech.icey.util.ManualDispose;
import tech.icey.util.Optional;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class CommandBuffer implements ManualDispose {
    public CommandBuffer(CommandPool commandPool, boolean primary, boolean oneTimeSubmit) {
        this.commandPool = commandPool;
        this.primary = primary;
        this.oneTimeSubmit = oneTimeSubmit;

        VkDevice vkDevice = commandPool.device().vkDevice();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo commandBufferAllocateInfo = VkCommandBufferAllocateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(commandPool.vkCommandPool())
                    .level(primary ? VK_COMMAND_BUFFER_LEVEL_PRIMARY : VK_COMMAND_BUFFER_LEVEL_SECONDARY)
                    .commandBufferCount(1);

            PointerBuffer cmdbufBuffer = stack.mallocPointer(1);
            int ret = vkAllocateCommandBuffers(vkDevice, commandBufferAllocateInfo, cmdbufBuffer);
            if (ret != VK_SUCCESS) {
                runtimeError("无法从指令池创建指令缓冲");
            }

            vkCommandBuffer = new VkCommandBuffer(cmdbufBuffer.get(0), vkDevice);
        }
    }

    public CommandPool commandPool() {
        assert !isDisposed;
        return commandPool;
    }

    public long vkCommandBuffer() {
        assert !isDisposed;
        return vkCommandBuffer.address();
    }

    public boolean primary() {
        assert !isDisposed;
        return primary;
    }

    public boolean oneTimeSubmit() {
        assert !isDisposed;
        return oneTimeSubmit;
    }

    public final class InheritanceInfo {
        public InheritanceInfo(long vkRenderPass, long vkFramebuffer, int subpass) {
            this.vkRenderPass = vkRenderPass;
            this.vkFramebuffer = vkFramebuffer;
            this.subpass = subpass;
        }

        public final long vkRenderPass;
        public final long vkFramebuffer;
        public final int subpass;
    }

    public void beginRecording() {
        beginRecording(Optional.none());
    }

    public void beginRecording(Optional<InheritanceInfo> inheritanceInfo) {
        assert !isDisposed;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo cmdBufBeginInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            if (oneTimeSubmit) {
                cmdBufBeginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            }

            if (!primary) {
                if (!(inheritanceInfo instanceof Optional.Some<InheritanceInfo> someInheritanceInfo)) {
                    runtimeError("记录次级指令缓冲时必须提供继承信息");
                    return;
                }

                VkCommandBufferInheritanceInfo vkInheritanceInfo = VkCommandBufferInheritanceInfo.calloc(stack)
                        .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO)
                        .renderPass(someInheritanceInfo.value.vkRenderPass)
                        .subpass(someInheritanceInfo.value.subpass)
                        .framebuffer(someInheritanceInfo.value.vkFramebuffer);

                cmdBufBeginInfo.pInheritanceInfo(vkInheritanceInfo);
                cmdBufBeginInfo.flags(VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT);
            }

            int ret = vkBeginCommandBuffer(vkCommandBuffer, cmdBufBeginInfo);
            if (ret != VK_SUCCESS) {
                runtimeError("无法开始录制指令缓冲");
            }
        }
    }

    public void endRecording() {
        assert !isDisposed;
        int ret = vkEndCommandBuffer(vkCommandBuffer);
        if (ret != VK_SUCCESS) {
            runtimeError("无法完成指令缓冲录制");
        }
    }

    public void reset() {
        assert !isDisposed;
        vkResetCommandBuffer(vkCommandBuffer, VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
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
        vkFreeCommandBuffers(commandPool.device().vkDevice(), commandPool.vkCommandPool(), vkCommandBuffer);
    }

    private final CommandPool commandPool;
    private final boolean oneTimeSubmit;
    private final boolean primary;
    private final VkCommandBuffer vkCommandBuffer;
    private volatile boolean isDisposed = false;
}
