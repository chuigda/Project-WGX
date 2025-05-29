package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import club.doki7.vulkan.command.VkDeviceCommands;
import club.doki7.vulkan.command.VkEntryCommands;
import club.doki7.vulkan.command.VkInstanceCommands;
import club.doki7.vulkan.command.VkStaticCommands;
import org.jetbrains.annotations.Nullable;
import club.doki7.glfw.GLFW;
import club.doki7.glfw.handle.GLFWwindow;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkCommandBufferUsageFlags;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.VkCommandBufferLevel;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vulkan.handle.*;
import club.doki7.vma.VMA;
import club.doki7.vma.handle.VmaAllocator;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.Arena;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public final class VulkanRenderEngineContext {
    public final Arena prefabArena = Arena.ofAuto();
    /// 同一时刻，最多只允许在渲染线程外额外提交 {@code 4} 个指令缓冲
    public final Semaphore graphicsQueueSubmitPermission = new Semaphore(4);

    public final VkStaticCommands sCmd;
    public final VkEntryCommands eCmd;
    public final VkInstanceCommands iCmd;
    public final VkDeviceCommands dCmd;
    public final VMA vma;

    public final VkPhysicalDevice physicalDevice;
    public final int graphicsQueueFamilyIndex;
    public final int presentQueueFamilyIndex;
    public final Option<Integer> dedicatedTransferQueueFamilyIndex;

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

    public boolean disposed = false;

    VulkanRenderEngineContext(
            VkStaticCommands sCmd,
            VkEntryCommands eCmd,
            VkInstanceCommands iCmd,
            VkDeviceCommands dCmd,
            VMA vma,

            VkPhysicalDevice physicalDevice,
            int graphicsQueueFamilyIndex,
            int presentQueueFamilyIndex,
            Option<Integer> dedicatedTransferQueueFamilyIndex,

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
            IntPtr buffer = IntPtr.allocate(arena, code);

            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.allocate(arena);
            createInfo.codeSize(buffer.size() * Integer.BYTES);
            createInfo.pCode(buffer);

            VkShaderModule.Ptr pShaderModule = VkShaderModule.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result = dCmd.createShaderModule(device, createInfo, null, pShaderModule);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建着色器模块, 错误代码: " + VkResult.explain(result));
            }
            return Objects.requireNonNull(pShaderModule.read());
        }
    }

    public void executeTransferCommand(Action1<VkCommandBuffer> recordCommandBuffer) throws RenderException {
        if (!(transferCommandPool instanceof Option.Some<VkCommandPool> someCommandPool)) {
            throw new IllegalStateException("未启用专用传输队列");
        }

        VkCommandPool commandPool1 = someCommandPool.value;
        VkQueue queue = dedicatedTransferQueue.get();
        executeOnceCommand(commandPool1, queue, recordCommandBuffer);
    }

    public void executeGraphicsCommand(Action1<VkCommandBuffer> recordCommandBuffer) throws RenderException {
        graphicsQueueSubmitPermission.acquireUninterruptibly();
        try {
            executeOnceCommand(graphicsOnceCommandPool, graphicsQueue, recordCommandBuffer);
        }
        finally {
            graphicsQueueSubmitPermission.release();
        }
    }

    public void waitDeviceIdle() {
        // Vulkan 文档表明 `vkDeviceWaitIdle` 需要外部同步所有从设备上创建的队列，妈的
        synchronized (graphicsQueue) {
            synchronized (presentQueue) {
                if (dedicatedTransferQueue instanceof Option.Some<VkQueue> someDedicatedTransferQueue) {
                    synchronized (someDedicatedTransferQueue.value) {
                        dCmd.deviceWaitIdle(device);
                    }
                }
                else {
                    dCmd.deviceWaitIdle(device);
                }
            }
        }
    }

    public synchronized void dispose() {
        this.disposed = true;

        waitDeviceIdle();

        vma.destroyAllocator(vmaAllocator);
        if (transferCommandPool instanceof Option.Some<VkCommandPool> someTransferCommandPool) {
            dCmd.destroyCommandPool(device, someTransferCommandPool.value, null);
        }
        dCmd.destroyCommandPool(device, commandPool, null);
        dCmd.destroyCommandPool(device, graphicsOnceCommandPool, null);
        for (VkFence fence : inFlightFences) {
            dCmd.destroyFence(device, fence, null);
        }
        for (VkSemaphore semaphore : renderFinishedSemaphores) {
            dCmd.destroySemaphore(device, semaphore, null);
        }
        for (VkSemaphore semaphore : imageAvailableSemaphores) {
            dCmd.destroySemaphore(device, semaphore, null);
        }
        dCmd.destroyDevice(device, null);
        if (debugMessenger instanceof Option.Some<VkDebugUtilsMessengerEXT> someDebugMessenger) {
            iCmd.destroyDebugUtilsMessengerEXT(instance, someDebugMessenger.value, null);
        }
        iCmd.destroySurfaceKHR(instance, surface, null);
        iCmd.destroyInstance(instance, null);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private void executeOnceCommand(
            VkCommandPool commandPool1,
            VkQueue queue,
            Action1<VkCommandBuffer> recordCommandBuffer
    ) throws RenderException {
        @Nullable VkCommandBuffer commandBuffer = null;
        @Nullable VkFence fence = null;

        try (Arena arena = Arena.ofConfined()) {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.allocate(arena);
            allocateInfo.commandPool(commandPool1);
            allocateInfo.level(VkCommandBufferLevel.PRIMARY);
            allocateInfo.commandBufferCount(1);

            VkCommandBuffer.Ptr pCommandBuffer = VkCommandBuffer.Ptr.allocate(arena);
            @EnumType(VkResult.class) int result;

            synchronized (commandPool1) {
                result = dCmd.allocateCommandBuffers(device, allocateInfo, pCommandBuffer);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法为操作分配指令缓冲, 错误代码: " + VkResult.explain(result));
                }
                commandBuffer = pCommandBuffer.read();

                VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.allocate(arena);
                beginInfo.flags(VkCommandBufferUsageFlags.ONE_TIME_SUBMIT);
                result = dCmd.beginCommandBuffer(commandBuffer, beginInfo);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法开始记录指令缓冲, 错误代码: " + VkResult.explain(result));
                }

                recordCommandBuffer.apply(commandBuffer);

                result = dCmd.endCommandBuffer(commandBuffer);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法结束指令缓冲记录, 错误代码: " + VkResult.explain(result));
                }
            }

            VkSubmitInfo submitInfo = VkSubmitInfo.allocate(arena);
            submitInfo.commandBufferCount(1);
            submitInfo.pCommandBuffers(pCommandBuffer);

            VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.allocate(arena);
            VkFence.Ptr pFence = VkFence.Ptr.allocate(arena);
            result = dCmd.createFence(device, fenceCreateInfo, null, pFence);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法创建指令缓冲同步信栅栏, 错误代码: " + VkResult.explain(result));
            }
            fence = pFence.read();

            synchronized (queue) {
                result = dCmd.queueSubmit(queue, 1, submitInfo, fence);
                if (result != VkResult.SUCCESS) {
                    throw new RenderException("无法提交指令缓冲, 错误代码: " + VkResult.explain(result));
                }
            }

            result = dCmd.waitForFences(device, 1, pFence, VkConstants.TRUE, -1);
            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法等待操作指令缓冲完成, 错误代码: " + VkResult.explain(result));
            }
        }
        finally {
            if (fence != null) {
                dCmd.destroyFence(device, fence, null);
            }

            if (commandBuffer != null) {
                try (Arena arena = Arena.ofConfined()) {
                    VkCommandBuffer.Ptr pCommandBuffer = VkCommandBuffer.Ptr.allocate(arena);
                    pCommandBuffer.write(commandBuffer);
                    dCmd.freeCommandBuffers(device, commandPool1, 1, pCommandBuffer);
                }
            }
        }
    }
}
