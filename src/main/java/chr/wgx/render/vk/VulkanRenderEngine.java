package chr.wgx.render.vk;

import chr.wgx.Config;
import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.handle.*;
import chr.wgx.render.info.*;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkAccessFlags;
import tech.icey.vk4j.bitmask.VkImageAspectFlags;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.VkCommandBufferBeginInfo;
import tech.icey.vk4j.datatype.VkImageMemoryBarrier;
import tech.icey.vk4j.datatype.VkPresentInfoKHR;
import tech.icey.vk4j.datatype.VkSubmitInfo;
import tech.icey.vk4j.enumtype.VkImageLayout;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.vk4j.handle.VkFence;
import tech.icey.vk4j.handle.VkSemaphore;
import tech.icey.vk4j.handle.VkSwapchainKHR;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action2;

import java.lang.foreign.Arena;
import java.util.logging.Logger;

public final class VulkanRenderEngine extends AbstractRenderEngine {
    public VulkanRenderEngine(
            Action0 onInit,
            Action2<Integer, Integer> onResize,
            Action0 onBeforeRenderFrame,
            Action0 onAfterRenderFrame,
            Action0 onClose
    ) {
        super(onInit, onResize, onBeforeRenderFrame, onAfterRenderFrame, onClose);
    }

    private Option<VulkanRenderEngineContext> engineContextOption = Option.none();
    private Option<Swapchain> swapchainOption = Option.none();
    private int currentFrameIndex = 0;

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
    protected void resize(int width, int height) throws RenderException {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> cx)) {
            return;
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> someCx)) {
            return;
        }
        VulkanRenderEngineContext cx = someCx.value;

        if (!(swapchainOption instanceof Option.Some<Swapchain> someSwapchain)) {
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
            if (result != VkResult.VK_SUCCESS) {
                // TODO: 处理 VK_SUBOPTIMAL_KHR 和 VK_ERROR_OUT_OF_DATE_KHR
                throw new RenderException("无法获取下一张交换链图像");
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
            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法提交交换链图像到队列, 错误代码: " + VkResult.explain(result));
            }
        }

        currentFrameIndex = (currentFrameIndex + 1) % Config.config().vulkanConfig.maxFramesInFlight;
    }

    @Override
    protected void close() {
        if (!(engineContextOption instanceof Option.Some<VulkanRenderEngineContext> cx)) {
            return;
        }
    }

    @Override
    public ObjectHandle createObject(ObjectCreateInfo info) throws RenderException {
        return null;
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
                throw new RenderException("无法开始录制指令缓冲, 错误代码: " + VkResult.explain(result));
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
                throw new RenderException("无法结束录制指令缓冲, 错误代码: " + VkResult.explain(result));
            }
        }
    }

    private static final Logger logger = Logger.getLogger(VulkanRenderEngine.class.getName());
}
