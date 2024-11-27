package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkCommandBufferUsageFlags;
import tech.icey.vk4j.bitmask.VkSampleCountFlags;
import tech.icey.vk4j.command.DeviceCommands;
import tech.icey.vk4j.command.EntryCommands;
import tech.icey.vk4j.command.InstanceCommands;
import tech.icey.vk4j.command.StaticCommands;
import tech.icey.vk4j.datatype.*;
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
    public final VkCommandPool graphicsOnceCommandPool;
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
            VkCommandPool graphicsOnceCommandPool,
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
        this.graphicsOnceCommandPool = graphicsOnceCommandPool;
        this.commandBuffers = commandBuffers;
        this.transferCommandPool = transferCommandPool;
    }

    public static VulkanRenderEngineContext create(GLFW glfw, GLFWwindow window) throws RenderException {
        return new VREContextInitialiser().init(glfw, window);
    }

    public VkShaderModule createShaderModule(byte[] code) throws RenderException {
        assert code.length % Integer.BYTES == 0;
        try (Arena arena = Arena.ofConfined()) {
            IntBuffer buffer = IntBuffer.allocate(arena, code);

            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.allocate(arena);
            createInfo.codeSize(buffer.size() * Integer.BYTES);
            createInfo.pCode(buffer);

            VkShaderModule.Buffer pShaderModule = VkShaderModule.Buffer.allocate(arena);
            @enumtype(VkResult.class) int result = dCmd.vkCreateShaderModule(device, createInfo, null, pShaderModule);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法创建着色器模块, 错误代码: " + VkResult.explain(result));
            }
            return pShaderModule.read();
        }
    }

    public void executeTransferCommand(Action1<VkCommandBuffer> recordCommandBuffer) throws RenderException {
        if (!(transferCommandPool instanceof Option.Some<VkCommandPool> someCommandPool)) {
            throw new IllegalStateException("未启用专用传输队列");
        }

        VkCommandPool commandPool1 = someCommandPool.value;
        VkQueue queue = dedicatedTransferQueue.get();

        executeOnceCommand(
                commandPool1,
                queue,
                recordCommandBuffer
        );
    }

    public void executeGraphicsCommand(Action1<VkCommandBuffer> recordCommandBuffer) throws RenderException {
        executeOnceCommand(
                graphicsOnceCommandPool,
                graphicsQueue,
                recordCommandBuffer
        );
    }

    public void dispose() {
        dCmd.vkDeviceWaitIdle(device);

        vma.vmaDestroyAllocator(vmaAllocator);
        if (transferCommandPool instanceof Option.Some<VkCommandPool> someTransferCommandPool) {
            dCmd.vkDestroyCommandPool(device, someTransferCommandPool.value, null);
        }
        dCmd.vkDestroyCommandPool(device, commandPool, null);
        dCmd.vkDestroyCommandPool(device, graphicsOnceCommandPool, null);
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
    private void executeOnceCommand(
            VkCommandPool commandPool1,
            VkQueue queue,
            Action1<VkCommandBuffer> recordCommandBuffer
    ) throws RenderException {
        VkCommandBuffer commandBuffer = null;
        VkFence fence = null;

        try (Arena arena = Arena.ofConfined()) {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.allocate(arena);
            allocateInfo.commandPool(commandPool1);
            allocateInfo.level(VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocateInfo.commandBufferCount(1);

            VkCommandBuffer.Buffer pCommandBuffer = VkCommandBuffer.Buffer.allocate(arena);
            @enumtype(VkResult.class) int result;

            synchronized (commandPool1) {
                result = dCmd.vkAllocateCommandBuffers(device, allocateInfo, pCommandBuffer);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法为操作分配指令缓冲, 错误代码: " + VkResult.explain(result));
                }
                commandBuffer = pCommandBuffer.read();

                VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.allocate(arena);
                beginInfo.flags(VkCommandBufferUsageFlags.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
                result = dCmd.vkBeginCommandBuffer(commandBuffer, beginInfo);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法开始记录指令缓冲, 错误代码: " + VkResult.explain(result));
                }

                recordCommandBuffer.apply(commandBuffer);

                result = dCmd.vkEndCommandBuffer(commandBuffer);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法结束指令缓冲记录, 错误代码: " + VkResult.explain(result));
                }
            }

            VkSubmitInfo submitInfo = VkSubmitInfo.allocate(arena);
            submitInfo.commandBufferCount(1);
            submitInfo.pCommandBuffers(pCommandBuffer);

            VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.allocate(arena);
            VkFence.Buffer pFence = VkFence.Buffer.allocate(arena);
            result = dCmd.vkCreateFence(device, fenceCreateInfo, null, pFence);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法创建指令缓冲同步信栅栏, 错误代码: " + VkResult.explain(result));
            }
            fence = pFence.read();

            synchronized (queue) {
                result = dCmd.vkQueueSubmit(queue, 1, submitInfo, fence);
                if (result != VkResult.VK_SUCCESS) {
                    throw new RenderException("无法提交指令缓冲, 错误代码: " + VkResult.explain(result));
                }
            }

            result = dCmd.vkWaitForFences(device, 1, pFence, Constants.VK_TRUE, -1);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法等待操作指令缓冲完成, 错误代码: " + VkResult.explain(result));
            }
        }
        finally {
            if (fence != null) {
                dCmd.vkDestroyFence(device, fence, null);
            }

            if (commandBuffer != null) {
                try (Arena arena = Arena.ofConfined()) {
                    VkCommandBuffer.Buffer pCommandBuffer = VkCommandBuffer.Buffer.allocate(arena);
                    pCommandBuffer.write(commandBuffer);
                    dCmd.vkFreeCommandBuffers(device, commandPool1, 1, pCommandBuffer);
                }
            }
        }
    }
}
