package chr.wgx.render.vk;

import chr.wgx.Config;
import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.data.Texture;
import chr.wgx.render.data.UniformBuffer;
import chr.wgx.render.info.*;
import chr.wgx.render.vk.data.*;
import tech.icey.glfw.GLFW;
import tech.icey.glfw.handle.GLFWwindow;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.*;
import tech.icey.vk4j.datatype.*;
import tech.icey.vk4j.enumtype.*;
import tech.icey.vk4j.handle.*;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.functional.Action0;
import tech.icey.xjbutil.functional.Action1;
import tech.icey.xjbutil.functional.Action2;

import java.awt.image.BufferedImage;
import java.lang.foreign.Arena;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        objectCreateAspect = new ObjectCreateAspect(this);
        attachmentCreateAspect = new AttachmentCreateAspect(this);
        pipelineCreateAspect = new PipelineCreateAspect(this);
    }

    @Override
    protected void init(GLFW glfw, GLFWwindow window) throws RenderException {
        cx = VulkanRenderEngineContext.create(glfw, window);

        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pWidthHeight = IntBuffer.allocate(arena, 2);
            glfw.glfwGetFramebufferSize(window, pWidthHeight, pWidthHeight.offset(1));

            int width = pWidthHeight.read(0);
            int height = pWidthHeight.read(1);
            swapchain = Swapchain.create(cx, width, height);
            logger.info("交换链已创建");
        } catch (RenderException e) {
            logger.severe("无法创建交换链: " + e.getMessage());
            logger.warning("程序会继续运行, 但渲染结果将不会被输出");
        }
    }

    @Override
    protected void resize(int width, int height) throws RenderException {
        if (width == 0 || height == 0) {
            pauseRender = true;
            return;
        }
        pauseRender = false;

        cx.waitDeviceIdle();
        swapchain.dispose(cx);

        try {
            swapchain = Swapchain.create(cx, width, height);
            logger.info("交换链已重新创建");
        } catch (RenderException e) {
            logger.severe("无法重新创建交换链: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void renderFrame() throws RenderException {
        if (pauseRender) {
            return;
        }

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

            synchronized (cx.graphicsQueue) {
                result = cx.dCmd.vkQueueSubmit(cx.graphicsQueue, 1, submitInfo, inFlightFence);
            }
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

            synchronized (
                    cx.graphicsQueue.segment().address() == cx.presentQueue.segment().address() ?
                            cx.graphicsQueue :
                            cx.presentQueue
            ) {
                result = cx.dCmd.vkQueuePresentKHR(cx.presentQueue, presentInfo);
            }

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
        cx.waitDeviceIdle();
        swapchain.dispose(cx);

        for (VulkanPipeline pipeline : pipelines) {
            pipeline.dispose(cx);
        }

        for (VulkanRenderObject object : objects) {
            object.dispose(cx);
        }

        cx.dispose();
    }

    @Override
    public RenderObject createObject(ObjectCreateInfo info) throws RenderException {
        return objectCreateAspect.createObjectImpl(info);
    }

    @Override
    public List<RenderObject> createObject(List<ObjectCreateInfo> infos) throws RenderException {
        return objectCreateAspect.createObjectImpl(infos);
    }

    @Override
    public Pair<Attachment, Texture> createColorAttachment(AttachmentCreateInfo info) throws RenderException {
        return attachmentCreateAspect.createColorAttachmentImpl(info);
    }

    @Override
    public Attachment createDepthAttachment(AttachmentCreateInfo info) throws RenderException {
        return attachmentCreateAspect.createDepthAttachmentImpl(info);
    }

    @Override
    public Pair<Attachment, Attachment> getDefaultAttachments() {
        return new Pair<>(DEFAULT_COLOR_ATTACHMENT, DEFAULT_DEPTH_ATTACHMENT);
    }

    @Override
    public Texture createTexture(BufferedImage image) throws RenderException {
        return null;
    }

    @Override
    public UniformBuffer createUniform(UniformBufferCreateInfo info) throws RenderException {
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

            // TODO: just very premature and temporary implementation, we need to implement task sorting and dependency resolution in further development
            VkRenderingAttachmentInfo attachmentInfo = VkRenderingAttachmentInfo.allocate(arena);
            attachmentInfo.imageView(swapchainImage.imageView);
            attachmentInfo.imageLayout(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            attachmentInfo.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR);
            attachmentInfo.storeOp(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE);
            VkRenderingAttachmentInfo depthAttachmentInfo = VkRenderingAttachmentInfo.allocate(arena);
            depthAttachmentInfo.imageView(swapchain.depthImage.imageView);
            depthAttachmentInfo.imageLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            depthAttachmentInfo.loadOp(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachmentInfo.storeOp(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachmentInfo.clearValue().depthStencil().depth(1.0f);

            VkRenderingInfo renderingInfo = VkRenderingInfo.allocate(arena);
            renderingInfo.renderArea().extent(swapchain.swapExtent);
            renderingInfo.layerCount(1);
            renderingInfo.colorAttachmentCount(1);
            renderingInfo.pColorAttachments(attachmentInfo);
            renderingInfo.pDepthAttachment(depthAttachmentInfo);
            VkViewport viewport = VkViewport.allocate(arena);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(swapchain.swapExtent.width());
            viewport.height(swapchain.swapExtent.height());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);
            VkRect2D scissor = VkRect2D.allocate(arena);
            scissor.offset().x(0);
            scissor.offset().y(0);
            scissor.extent(swapchain.swapExtent);

            cx.dCmd.vkCmdBeginRendering(commandBuffer, renderingInfo);
            cx.dCmd.vkCmdSetViewport(commandBuffer, 0, 1, viewport);
            cx.dCmd.vkCmdSetScissor(commandBuffer, 0, 1, scissor);

//            for (RenderTaskInfo task : tasks.values()) {
//                Resource.Pipeline pipeline = Objects.requireNonNull(pipelines.get(task.pipelineHandle.getId()));
//
//                cx.dCmd.vkCmdBindPipeline(commandBuffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.pipeline);
//                VkBuffer.Buffer pVertexBuffer = VkBuffer.Buffer.allocate(arena);
//                LongBuffer pOffsets = LongBuffer.allocate(arena);
//                for (int i = 0; i < task.objectHandles.size(); i++) {
//                    Resource.Object object = Objects.requireNonNull(objects.get(task.objectHandles.get(i).getId()));
//
//                    pVertexBuffer.write(object.buffer.buffer);
//                    cx.dCmd.vkCmdBindVertexBuffers(commandBuffer, 0, 1, pVertexBuffer, pOffsets);
//                    cx.dCmd.vkCmdDraw(commandBuffer, (int) object.vertexCount, 1, 0, 0);
//                }
//            }

            cx.dCmd.vkCmdEndRendering(commandBuffer);

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

    private final ObjectCreateAspect objectCreateAspect;
    private final AttachmentCreateAspect attachmentCreateAspect;
    private final PipelineCreateAspect pipelineCreateAspect;

    VulkanRenderEngineContext cx;
    Swapchain swapchain;
    int currentFrameIndex = 0;
    boolean pauseRender = false;

    final Set<VulkanRenderObject> objects = ConcurrentHashMap.newKeySet();
    final Set<VulkanPipeline> pipelines = ConcurrentHashMap.newKeySet();
    final Set<ImageAttachment> colorAttachments = ConcurrentHashMap.newKeySet();
    final Set<ImageAttachment> depthAttachments = ConcurrentHashMap.newKeySet();
    final Set<Texture> textures = ConcurrentHashMap.newKeySet();

    static final SwapchainColorAttachment DEFAULT_COLOR_ATTACHMENT = new SwapchainColorAttachment();
    static final SwapchainDepthAttachment DEFAULT_DEPTH_ATTACHMENT = new SwapchainDepthAttachment();

    private static final Logger logger = Logger.getLogger(VulkanRenderEngine.class.getName());
}
