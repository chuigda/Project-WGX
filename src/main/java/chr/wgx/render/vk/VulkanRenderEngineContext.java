package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.annotation.enumtype;
import tech.icey.vk4j.bitmask.VkCommandBufferUsageFlags;
import tech.icey.vk4j.bitmask.VkSampleCountFlags;
import tech.icey.vk4j.command.DeviceCommands;
import tech.icey.vk4j.command.EntryCommands;
import tech.icey.vk4j.command.InstanceCommands;
import tech.icey.vk4j.command.StaticCommands;
import tech.icey.vk4j.datatype.VkCommandBufferAllocateInfo;
import tech.icey.vk4j.datatype.VkCommandBufferBeginInfo;
import tech.icey.vk4j.datatype.VkSubmitInfo;
import tech.icey.vk4j.enumtype.VkCommandBufferLevel;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.*;
import tech.icey.vma.VMA;
import tech.icey.vma.handle.VmaAllocator;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.Arena;

public final class VulkanRenderEngineContext {
    public final Arena autoArena = Arena.ofAuto();

    public final StaticCommands sCmd;
    public final EntryCommands eCmd;
    public final InstanceCommands iCmd;
    public final DeviceCommands dCmd;
    public final VMA vma;

    public final VkPhysicalDevice physicalDevice;
    public final int graphicsQueueFamilyIndex;
    public final int presentQueueFamilyIndex;
    public final Option<Integer> dedicatedTransferQueueFamilyIndex;
    public final @enumtype(VkSampleCountFlags.class) int msaaSampleCountFlags;

    public final VkInstance instance;
    public final Option<VkDebugUtilsMessengerEXT> debugMessenger;
    public final VkSurfaceKHR surface;
    public final VkDevice device;
    public final VkQueue graphicsQueue;
    public final VkQueue presentQueue;
    public final Option<VkQueue> dedicatedTransferQueue;
    public final VmaAllocator vmaAllocator;
    public final VkSemaphore[] imageAvailableSemaphores;
    public final VkSemaphore[] renderFinishedSemaphores;
    public final VkFence[] inFlightFences;
    public final VkCommandPool commandPool;
    public final VkCommandBuffer[] commandBuffers;
    public final Option<VkCommandPool> transferCommandPool;

    VulkanRenderEngineContext(
            StaticCommands sCmd,
            EntryCommands eCmd,
            InstanceCommands iCmd,
            DeviceCommands dCmd,
            VMA vma,

            VkPhysicalDevice physicalDevice,
            int graphicsQueueFamilyIndex,
            int presentQueueFamilyIndex,
            Option<Integer> dedicatedTransferQueueFamilyIndex,
            @enumtype(VkSampleCountFlags.class) int msaaSampleCount,

            VkInstance instance,
            Option<VkDebugUtilsMessengerEXT> debugMessenger,
            VkSurfaceKHR surface,
            VkDevice device,
            VkQueue graphicsQueue,
            VkQueue presentQueue,
            Option<VkQueue> dedicatedTransferQueue,
            VmaAllocator vmaAllocator,
            VkSemaphore[] imageAvailableSemaphores,
            VkSemaphore[] renderFinishedSemaphores,
            VkFence[] inFlightFences,
            VkCommandPool commandPool,
            VkCommandBuffer[] commandBuffers,
            Option<VkCommandPool> transferCommandPool
    ) {
        this.sCmd = sCmd;
        this.eCmd = eCmd;
        this.iCmd = iCmd;
        this.dCmd = dCmd;
        this.vma = vma;

        this.physicalDevice = physicalDevice;
        this.graphicsQueueFamilyIndex = graphicsQueueFamilyIndex;
        this.presentQueueFamilyIndex = presentQueueFamilyIndex;
        this.dedicatedTransferQueueFamilyIndex = dedicatedTransferQueueFamilyIndex;
        this.msaaSampleCountFlags = msaaSampleCount;

        this.instance = instance;
        this.debugMessenger = debugMessenger;
        this.surface = surface;
        this.device = device;
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;
        this.dedicatedTransferQueue = dedicatedTransferQueue;
        this.vmaAllocator = vmaAllocator;
        this.imageAvailableSemaphores = imageAvailableSemaphores;
        this.renderFinishedSemaphores = renderFinishedSemaphores;
        this.inFlightFences = inFlightFences;
        this.commandPool = commandPool;
        this.commandBuffers = commandBuffers;
        this.transferCommandPool = transferCommandPool;
    }

    public static VulkanRenderEngineContext create(GLFW glfw, GLFWwindow window) throws RenderException {
        return new VREContextInitialiser().init(glfw, window);
    }

    public void executeTransferCommand(
            Action1<VkCommandBuffer> recordCommandBuffer,
            Option<VkSemaphore.Buffer> pWaitSemaphores,
            Option<VkSemaphore.Buffer> pSignalSemaphores,
            Option<VkFence> fence,
            boolean waitQueueIdle
    ) throws RenderException {
        if (!(transferCommandPool instanceof Option.Some<VkCommandPool> someCommandPool)) {
            throw new IllegalStateException("未启用专用传输队列");
        }

        VkCommandPool commandPool1 = someCommandPool.value;
        VkQueue queue = dedicatedTransferQueue.get();

        executeOnceCommand(
                commandPool1,
                queue,
                recordCommandBuffer,
                pWaitSemaphores,
                pSignalSemaphores,
                fence,
                waitQueueIdle
        );
    }

    public Option<VkCommandBuffer> executeGraphicsCommand(
            Action1<VkCommandBuffer> recordCommandBuffer,
            Option<VkSemaphore.Buffer> pWaitSemaphores,
            Option<VkSemaphore.Buffer> pSignalSemaphores,
            Option<VkFence> fence,
            boolean waitQueueIdle
    ) throws RenderException {
        return executeOnceCommand(
                commandPool,
                graphicsQueue,
                recordCommandBuffer,
                pWaitSemaphores,
                pSignalSemaphores,
                fence,
                waitQueueIdle
        );
    }

    public void dispose() {
        dCmd.vkDeviceWaitIdle(device);

        vma.vmaDestroyAllocator(vmaAllocator);
        if (transferCommandPool instanceof Option.Some<VkCommandPool> someTransferCommandPool) {
            dCmd.vkDestroyCommandPool(device, someTransferCommandPool.value, null);
        }
        dCmd.vkDestroyCommandPool(device, commandPool, null);
        for (VkFence fence : inFlightFences) {
            dCmd.vkDestroyFence(device, fence, null);
        }
        for (VkSemaphore semaphore : renderFinishedSemaphores) {
            dCmd.vkDestroySemaphore(device, semaphore, null);
        }
        for (VkSemaphore semaphore : imageAvailableSemaphores) {
            dCmd.vkDestroySemaphore(device, semaphore, null);
        }
        dCmd.vkDestroyDevice(device, null);
        if (debugMessenger instanceof Option.Some<VkDebugUtilsMessengerEXT> someDebugMessenger) {
            iCmd.vkDestroyDebugUtilsMessengerEXT(instance, someDebugMessenger.value, null);
        }
        iCmd.vkDestroySurfaceKHR(instance, surface, null);
        iCmd.vkDestroyInstance(instance, null);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private Option<VkCommandBuffer> executeOnceCommand(
            VkCommandPool commandPool1,
            VkQueue queue,
            Action1<VkCommandBuffer> recordCommandBuffer,
            Option<VkSemaphore.Buffer> pWaitSemaphores,
            Option<VkSemaphore.Buffer> pSignalSemaphores,
            Option<VkFence> fence,
            boolean waitQueueIdle
    ) throws RenderException {
        try (Arena arena = Arena.ofConfined()) {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.allocate(arena);
            allocateInfo.commandPool(commandPool1);
            allocateInfo.level(VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocateInfo.commandBufferCount(1);

            VkCommandBuffer.Buffer pCommandBuffer = VkCommandBuffer.Buffer.allocate(arena);
            VkCommandBuffer commandBuffer;
            @enumtype(VkResult.class) int result;

            synchronized (commandPool1) {
                result = dCmd.vkAllocateCommandBuffers(device, allocateInfo, pCommandBuffer);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法为传输操作分配指令缓冲, 错误代码: " + VkResult.explain(result));
                }
                commandBuffer = pCommandBuffer.read();

                VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.allocate(arena);
                beginInfo.flags(VkCommandBufferUsageFlags.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
                result = dCmd.vkBeginCommandBuffer(commandBuffer, beginInfo);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法开始记录传输操作指令缓冲, 错误代码: " + VkResult.explain(result));
                }

                recordCommandBuffer.apply(commandBuffer);

                result = dCmd.vkEndCommandBuffer(commandBuffer);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法结束传输操作指令缓冲记录, 错误代码: " + VkResult.explain(result));
                }
            }

            VkSubmitInfo submitInfo = VkSubmitInfo.allocate(arena);
            submitInfo.commandBufferCount(1);
            submitInfo.pCommandBuffers(pCommandBuffer);
            if (pWaitSemaphores instanceof Option.Some<VkSemaphore.Buffer> someWaitSemaphores) {
                submitInfo.waitSemaphoreCount((int) someWaitSemaphores.value.size());
                submitInfo.pWaitSemaphores(someWaitSemaphores.value);
            }
            if (pSignalSemaphores instanceof Option.Some<VkSemaphore.Buffer> someSignalSemaphores) {
                submitInfo.signalSemaphoreCount((int) someSignalSemaphores.value.size());
                submitInfo.pSignalSemaphores(someSignalSemaphores.value);
            }

            synchronized (queue) {
                result = dCmd.vkQueueSubmit(queue, 1, submitInfo, fence.nullable());
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法提交传输操作指令缓冲, 错误代码: " + VkResult.explain(result));
                }

                if (waitQueueIdle) {
                    dCmd.vkQueueWaitIdle(queue);
                }
            }

            if (waitQueueIdle) {
                synchronized (commandPool1) {
                    dCmd.vkFreeCommandBuffers(device, commandPool1, 1, pCommandBuffer);
                }
                return Option.none();
            } else {
                return Option.some(commandBuffer);
            }
        }
    }
}
