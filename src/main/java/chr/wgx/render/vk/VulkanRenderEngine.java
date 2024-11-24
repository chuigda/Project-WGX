package chr.wgx.render.vk;

import chr.wgx.Config;
import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import org.jetbrains.annotations.Nullable;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.panama.buffer.PointerBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkAccessFlags;
import tech.icey.vk4j.bitmask.VkBufferUsageFlags;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.vk4j.handle.VkFence;
import tech.icey.vk4j.handle.VkSemaphore;
import tech.icey.vk4j.handle.VkSwapchainKHR;
import tech.icey.vma.bitmask.VmaAllocationCreateFlags;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;
import tech.icey.xjbutil.sync.Oneshot;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class VulkanRenderEngine extends AbstractRenderEngine {
    public VulkanRenderEngine(
            Action1<AbstractRenderEngine> onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        super(onInit, onResize, onBeforeRenderFrame, onAfterRenderFrame, onClose);
    }

    @Override
    protected void init(GLFW glfw, GLFWwindow window) throws RenderException {
        VulkanRenderEngineContext cx = VulkanRenderEngineContext.create(glfw, window);
        engineContextOption = Option.some(cx);

        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pWidthHeight = IntBuffer.allocate(arena, 2);
            glfw.glfwGetFramebufferSize(window, pWidthHeight, pWidthHeight.offset(1));

            int width = pWidthHeight.read(0);
            int height = pWidthHeight.read(1);
            swapchainOption = Option.some(Swapchain.create(cx, width, height));
            logger.info("交换链已创建");
        } catch (RenderException e) {
            logger.severe("无法创建交换链: " + e.getMessage());
            logger.warning("程序会继续运行, 但渲染结果将不会被输出");
        }
    }

    @Override
    protected void resize(int width, int height) {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> someCx)) {
            return;
        }
        VulkanRenderEngineContext cx = someCx.value;

        if (width == 0 || height == 0) {
            pauseRender = true;
            return;
        }
        pauseRender = false;

        cx.dCmd.vkDeviceWaitIdle(cx.device);
        if (swapchainOption instanceof Option.Some<Swapchain> someSwapchain) {
            someSwapchain.value.dispose(cx);
        }

        try {
            swapchainOption = Option.some(Swapchain.create(cx, width, height));
            logger.info("交换链已重新创建");
        } catch (RenderException e) {
            logger.severe("无法重新创建交换链: " + e.getMessage());
            logger.warning("程序会继续运行, 但渲染结果将不会被输出");
            swapchainOption = Option.none();
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> someCx)) {
            return;
        }
        VulkanRenderEngineContext cx = someCx.value;

        queueTransferAcquireObjects(cx);

        if (!(swapchainOption instanceof Option.Some<Swapchain> someSwapchain)) {
            return;
        }

        if (pauseRender) {
            return;
        }

        Swapchain swapchain = someSwapchain.value;
        VkFence inFlightFence = cx.inFlightFences[currentFrameIndex];
        VkSemaphore imageAvailableSemaphore = cx.imageAvailableSemaphores[currentFrameIndex];
        VkSemaphore renderFinishedSemaphore = cx.renderFinishedSemaphores[currentFrameIndex];
        VkCommandBuffer commandBuffer = cx.commandBuffers[currentFrameIndex];

        try (Arena arena = Arena.ofConfined()) {
            VkFence.Buffer pFence = VkFence.Buffer.allocate(arena);
            pFence.write(inFlightFence);
            cx.dCmd.vkWaitForFences(cx.device, 1, pFence, Constants.VK_TRUE, NativeLayout.UINT64_MAX);

            IntBuffer pImageIndex = IntBuffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.dCmd.vkAcquireNextImageKHR(
                    cx.device,
                    swapchain.vkSwapchain,
                    NativeLayout.UINT64_MAX,
                    imageAvailableSemaphore,
                    null,
                    pImageIndex
            );
            if (result == VkResult.VK_ERROR_OUT_OF_DATE_KHR) {
                return;
            }
            if (result != VkResult.VK_SUCCESS && result != VkResult.VK_SUBOPTIMAL_KHR) {
                throw new RenderException("无法获取交换链图像, 错误代码: " + VkResult.explain(result));
            }
            cx.dCmd.vkResetFences(cx.device, 1, pFence);

            int imageIndex = pImageIndex.read(0);
            resetAndRecordCommandBuffer(cx, commandBuffer, swapchain.swapchainImages[imageIndex]);

            VkSemaphore.Buffer pWaitSemaphore = VkSemaphore.Buffer.allocate(arena);
            pWaitSemaphore.write(imageAvailableSemaphore);
            IntBuffer pWaitStages = IntBuffer.allocate(arena);
            pWaitStages.write(VkPipelineStageFlags.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            VkCommandBuffer.Buffer pCommandBuffer = VkCommandBuffer.Buffer.allocate(arena);
            pCommandBuffer.write(commandBuffer);
            VkSemaphore.Buffer pSignalSemaphore = VkSemaphore.Buffer.allocate(arena);
            pSignalSemaphore.write(renderFinishedSemaphore);

            VkSubmitInfo submitInfo = VkSubmitInfo.allocate(arena);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(pWaitSemaphore);
            submitInfo.pWaitDstStageMask(pWaitStages);
            submitInfo.commandBufferCount(1);
            submitInfo.pCommandBuffers(pCommandBuffer);
            submitInfo.signalSemaphoreCount(1);
            submitInfo.pSignalSemaphores(pSignalSemaphore);

            result = cx.dCmd.vkQueueSubmit(cx.graphicsQueue, 1, submitInfo, inFlightFence);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法提交指令缓冲到队列, 错误代码: " + VkResult.explain(result));
            }

            VkSwapchainKHR.Buffer pSwapchain = VkSwapchainKHR.Buffer.allocate(arena);
            pSwapchain.write(swapchain.vkSwapchain);

            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.allocate(arena);
            presentInfo.waitSemaphoreCount(1);
            presentInfo.pWaitSemaphores(pSignalSemaphore);
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(pSwapchain);
            presentInfo.pImageIndices(pImageIndex);

            result = cx.dCmd.vkQueuePresentKHR(cx.presentQueue, presentInfo);
            if (result == VkResult.VK_ERROR_OUT_OF_DATE_KHR) {
                return;
            } else if (result != VkResult.VK_SUCCESS && result != VkResult.VK_SUBOPTIMAL_KHR) {
                throw new RenderException("无法提交交换链图像到队列, 错误代码: " + VkResult.explain(result));
            }
        }

        currentFrameIndex = (currentFrameIndex + 1) % Config.config().vulkanConfig.maxFramesInFlight;
    }

    @Override
    protected void close() {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> someCx)) {
            return;
        }
        VulkanRenderEngineContext cx = someCx.value;

        if (swapchainOption instanceof Option.Some<Swapchain> someSwapchain) {
            someSwapchain.value.dispose(someCx.value);
        }

        for (Resource.Object object : objects.values()) {
            object.dispose(cx);
        }

        cx.dispose();
    }

    @Override
    public ObjectHandle createObject(ObjectCreateInfo info) throws RenderException {
        long bufferSize = info.pData.segment().byteSize();
        assert bufferSize % info.vertexInputInfo.stride == 0;

        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> someCx)) {
            throw new RenderException("渲染引擎未初始化");
        }
        VulkanRenderEngineContext cx = someCx.value;

        try (Arena arena = Arena.ofConfined()) {
            Resource.Buffer stagingBuffer = Resource.Buffer.create(
                    cx,
                    bufferSize,
                    VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                    null
            );

            PointerBuffer ppData = PointerBuffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.vma.vmaMapMemory(
                    cx.vmaAllocator,
                    stagingBuffer.allocation,
                    ppData
            );
            if (result != VkResult.VK_SUCCESS) {
                stagingBuffer.dispose(cx);
                throw new RenderException("无法映射缓冲区内存, 错误代码: " + VkResult.explain(result));
            }
            MemorySegment pData = ppData.read().reinterpret(bufferSize);
            pData.copyFrom(info.pData.segment());
            cx.vma.vmaUnmapMemory(cx.vmaAllocator, stagingBuffer.allocation);

            Resource.Buffer vertexBuffer = Resource.Buffer.create(
                    cx,
                    bufferSize,
                    VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                    | VkBufferUsageFlags.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    0,
                    null
            );

            cx.executeTransferCommand(cmd -> {
                VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                copyRegion.size(bufferSize);
                cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, vertexBuffer.buffer, 1, copyRegion);

                VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                barrier.buffer(vertexBuffer.buffer);
                barrier.offset(0);
                barrier.size(bufferSize);
                cx.dCmd.vkCmdPipelineBarrier(
                        cmd,
                        VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                        VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                        0,
                        0, null,
                        1, barrier,
                        0, null
                );
            }, Option.none(), Option.none(), Option.none(), true);

            Pair<Oneshot.Sender<Boolean>, Oneshot.Receiver<Boolean>> channel = Oneshot.create();
            synchronized (unacquiredObjects) {
                unacquiredObjects.value.add(new UnacquiredObject(
                        vertexBuffer,
                        bufferSize,
                        channel.first()
                ));
            }

            if (!channel.second().recv()) {
                throw new RenderException("缓冲区传输失败");
            }
            stagingBuffer.dispose(cx);

            long handle = nextHandle();
            synchronized (objects) {
                objects.put(handle, new Resource.Object(vertexBuffer, info.vertexInputInfo));
            }
            return new ObjectHandle(handle);
        }
    }

    @Override
    public AttachmentHandle.Color createColorAttachment(AttachmentCreateInfo.Color info) throws RenderException {
        return null;
    }

    @Override
    public AttachmentHandle.Depth createDepthAttachment(AttachmentCreateInfo.Depth info) throws RenderException {
        return null;
    }

    @Override
    public UniformHandle.Sampler2D createTexture(TextureCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public UniformHandle createUniform(UniformCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public RenderPipelineHandle createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        return null;
    }

    @Override
    public RenderTaskHandle createTask(RenderTaskCreateInfo info) throws RenderException {
        return null;
    }

    private void handleObjectUploading(VulkanRenderEngineContext cx) {
        if (cx.dedicatedTransferQueue.isSome()) {
            queueTransferAcquireObjects(cx);
        }
        else {
            throw new RuntimeException("此功能尚未实现");
        }
    }

    private void queueTransferAcquireObjects(VulkanRenderEngineContext cx) {
        if (hasTransferAcquireJob.getAndSet(true)) {
            return;
        }

        List<UnacquiredObject> objectsToAcquire;
        synchronized (unacquiredObjects) {
            objectsToAcquire = unacquiredObjects.value;
            if (objectsToAcquire.isEmpty()) {
                hasTransferAcquireJob.set(false);
                return;
            }
            unacquiredObjects.value = new ArrayList<>();
        }

        VkFence.Buffer pFence = VkFence.Buffer.allocate(cx.autoArena);
        VkFence fence = null;
        try (Arena arena = Arena.ofConfined()) {
            VkFenceCreateInfo fenceCreateInfo = VkFenceCreateInfo.allocate(arena);
            @enumtype(VkResult.class) int result = cx.dCmd.vkCreateFence(cx.device, fenceCreateInfo, null, pFence);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法创建围栏, 错误代码: " + VkResult.explain(result));
            }
            fence = pFence.read();

            Option<VkCommandBuffer> acquireCommandBuffer = cx.executeGraphicsCommand(cmd -> {
                for (UnacquiredObject object : objectsToAcquire) {
                    VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                    barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                    barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                    barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                    barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                    barrier.buffer(object.buffer.buffer);
                    barrier.offset(0);
                    barrier.size(object.bufferSize);
                    cx.dCmd.vkCmdPipelineBarrier(
                            cmd,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            0,
                            0, null,
                            1, barrier,
                            0, null
                    );
                }
            }, Option.none(), Option.none(), Option.some(fence), false);

            VkFence fence1 = fence;
            new Thread(() -> {
                // TODO handle VK_DEVICE_LOST?
                cx.dCmd.vkWaitForFences(cx.device, 1, pFence, Constants.VK_TRUE, NativeLayout.UINT64_MAX);
                for (UnacquiredObject object : objectsToAcquire) {
                    object.onTransferComplete.send(true);
                }
                cx.dCmd.vkDestroyFence(cx.device, fence1, null);

                try (Arena arena1 = Arena.ofConfined()) {
                    VkCommandBuffer.Buffer pCommandBuffer = VkCommandBuffer.Buffer.allocate(arena1);
                    pCommandBuffer.write(acquireCommandBuffer.get());

                    synchronized (cx.commandPool) {
                        cx.dCmd.vkFreeCommandBuffers(cx.device, cx.commandPool, 1, pCommandBuffer);
                    }
                }
            }).start();
        } catch (RenderException e) {
            logger.severe("无法执行缓冲区传输任务: " + e.getMessage());
            for (UnacquiredObject object : objectsToAcquire) {
                object.onTransferComplete.send(false);
                object.buffer.dispose(cx);
            }

            if (fence != null) {
                cx.dCmd.vkDestroyFence(cx.device, fence, null);
            }
        }
    }

    private void resetAndRecordCommandBuffer(
            VulkanRenderEngineContext cx,
            VkCommandBuffer commandBuffer,
            Resource.SwapchainImage swapchainImage
    ) throws RenderException {
        cx.dCmd.vkResetCommandBuffer(commandBuffer, 0);

        try (Arena arena = Arena.ofConfined()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.allocate(arena);
            @enumtype(VkResult.class) int result = cx.dCmd.vkBeginCommandBuffer(commandBuffer, beginInfo);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法开始记录指令缓冲, 错误代码: " + VkResult.explain(result));
            }

            VkImageMemoryBarrier presentToDrawBarrier = VkImageMemoryBarrier.allocate(arena);
            presentToDrawBarrier.srcAccessMask(0);
            presentToDrawBarrier.dstAccessMask(VkAccessFlags.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
            presentToDrawBarrier.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
            presentToDrawBarrier.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            presentToDrawBarrier.image(swapchainImage.image);
            presentToDrawBarrier.subresourceRange().aspectMask(VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT);
            presentToDrawBarrier.subresourceRange().baseMipLevel(0);
            presentToDrawBarrier.subresourceRange().levelCount(1);
            presentToDrawBarrier.subresourceRange().baseArrayLayer(0);
            presentToDrawBarrier.subresourceRange().layerCount(1);
            cx.dCmd.vkCmdPipelineBarrier(
                    commandBuffer,
                    VkPipelineStageFlags.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                    VkPipelineStageFlags.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                    0,
                    0, null,
                    0, null,
                    1, presentToDrawBarrier
            );

            // TODO: insert rendering command here

            VkImageMemoryBarrier drawToPresentBarrier = VkImageMemoryBarrier.allocate(arena);
            drawToPresentBarrier.srcAccessMask(VkAccessFlags.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
            drawToPresentBarrier.dstAccessMask(0);
            drawToPresentBarrier.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            drawToPresentBarrier.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
            drawToPresentBarrier.image(swapchainImage.image);
            drawToPresentBarrier.subresourceRange().aspectMask(VkImageAspectFlags.VK_IMAGE_ASPECT_COLOR_BIT);
            drawToPresentBarrier.subresourceRange().baseMipLevel(0);
            drawToPresentBarrier.subresourceRange().levelCount(1);
            drawToPresentBarrier.subresourceRange().baseArrayLayer(0);
            drawToPresentBarrier.subresourceRange().layerCount(1);
            cx.dCmd.vkCmdPipelineBarrier(
                    commandBuffer,
                    VkPipelineStageFlags.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT,
                    VkPipelineStageFlags.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT,
                    0,
                    0, null,
                    0, null,
                    1, drawToPresentBarrier
            );

            result = cx.dCmd.vkEndCommandBuffer(commandBuffer);
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法结束指令缓冲记录, 错误代码: " + VkResult.explain(result));
            }
        }
    }

    private Option<VulkanRenderEngineContext> engineContextOption = Option.none();
    private Option<Swapchain> swapchainOption = Option.none();
    private int currentFrameIndex = 0;
    private boolean pauseRender = false;

    @SuppressWarnings("ClassCanBeRecord")
    private static final class UnacquiredObject {
        public final Resource.Buffer buffer;
        public final long bufferSize;
        public final Oneshot.Sender<Boolean> onTransferComplete;

        UnacquiredObject(
                Resource.Buffer buffer,
                long bufferSize,
                Oneshot.Sender<Boolean> onTransferComplete
        ) {
            this.buffer = buffer;
            this.bufferSize = bufferSize;
            this.onTransferComplete = onTransferComplete;
        }
    }

    private final Ref<List<UnacquiredObject>> unacquiredObjects = new Ref<>(new ArrayList<>());
    private final AtomicBoolean hasTransferAcquireJob = new AtomicBoolean(false);
    private final HashMap<Long, Resource.Object> objects = new HashMap<>();

    private static final Logger logger = Logger.getLogger(VulkanRenderEngine.class.getName());
}
