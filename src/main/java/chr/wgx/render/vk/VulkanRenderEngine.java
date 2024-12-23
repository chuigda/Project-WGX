package chr.wgx.render.vk;

import chr.wgx.config.Config;
import chr.wgx.render.AbstractRenderEngine;
import chr.wgx.render.RenderException;
import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.data.*;
import chr.wgx.render.info.*;
import chr.wgx.render.vk.compiled.CompiledRenderPassOp;
import chr.wgx.render.vk.data.*;
import chr.wgx.render.vk.task.VulkanRenderPass;
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
import java.util.concurrent.ConcurrentSkipListSet;
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
        objectCreateAspect = new ASPECT_ObjectCreate(this);
        attachmentCreateAspect = new ASPECT_AttachmentCreate(this);
        uniformCreateAspect = new ASPECT_UniformCreate(this);
        descriptorSetLayoutCreateAspect = new ASPECT_DescriptorSetLayoutCreate(this);
        descriptorSetCreateAspect = new ASPECT_DescriptorSetCreate(this);
        pipelineCreateAspect = new ASPECT_PipelineCreate(this);
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

        if (uniformManuallyUpdated.getAndSet(false)) {
            cx.dCmd.vkDeviceWaitIdle(cx.device);
            for (VulkanUniformBuffer uniform : manuallyUpdatedUniforms) {
                uniform.updateGPU();
            }
        }

        for (VulkanUniformBuffer uniform : framelyUpdatedUniforms) {
            uniform.updateGPU(currentFrameIndex);
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

        for (VulkanRenderPipeline pipeline : pipelines) {
            pipeline.dispose(cx);
        }

        for (VulkanRenderObject object : objects) {
            object.dispose(cx);
        }

        for (VulkanImageAttachment attachment : colorAttachments) {
            attachment.dispose(cx);
        }

        for (VulkanImageAttachment attachment : depthAttachments) {
            attachment.dispose(cx);
        }

        for (CombinedImageSampler texture : textures) {
            texture.dispose(cx);
        }

        for (VulkanUniformBuffer uniform : framelyUpdatedUniforms) {
            uniform.dispose(cx);
        }

        for (VulkanUniformBuffer uniform : manuallyUpdatedUniforms) {
            uniform.dispose(cx);
        }

        for (Map.Entry<VulkanDescriptorSetLayout, VkDescriptorPool> entry : descriptorPools.entrySet()) {
            cx.dCmd.vkDestroyDescriptorPool(cx.device, entry.getValue(), null);
            entry.getKey().dispose(cx);
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
        return new Pair<>(swapchainColorAttachment, swapchainDepthAttachment);
    }

    @Override
    public Texture createTexture(BufferedImage image) throws RenderException {
        return null;
    }

    @Override
    public UniformBuffer createUniform(UniformBufferCreateInfo info) throws RenderException {
        return uniformCreateAspect.createUniformImpl(info);
    }

    @Override
    public DescriptorSetLayout createDescriptorSetLayout(
            DescriptorSetLayoutCreateInfo info,
            int maxSets
    ) throws RenderException {
        return descriptorSetLayoutCreateAspect.createDescriptorSetLayoutImpl(info, maxSets);
    }

    @Override
    public DescriptorSet createDescriptorSet(DescriptorSetCreateInfo info) throws RenderException {
        return descriptorSetCreateAspect.createDescriptorSetImpl(info);
    }

    @Override
    public RenderPipeline createPipeline(RenderPipelineCreateInfo info) throws RenderException {
        return pipelineCreateAspect.createPipelineImpl(info);
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

            for (CompiledRenderPassOp op : compiledRenderPassOps) {
                op.recordToCommandBuffer(cx, commandBuffer, currentFrameIndex);
            }

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

    private final ASPECT_ObjectCreate objectCreateAspect;
    private final ASPECT_AttachmentCreate attachmentCreateAspect;
    private final ASPECT_UniformCreate uniformCreateAspect;
    private final ASPECT_DescriptorSetLayoutCreate descriptorSetLayoutCreateAspect;
    private final ASPECT_DescriptorSetCreate descriptorSetCreateAspect;
    private final ASPECT_PipelineCreate pipelineCreateAspect;

    // TODO resolve the nullability issue of cx/swapchain
    VulkanRenderEngineContext cx;
    Swapchain swapchain;
    int currentFrameIndex = 0;
    boolean pauseRender = false;

    final Set<VulkanRenderObject> objects = ConcurrentHashMap.newKeySet();
    final Set<VulkanRenderPipeline> pipelines = ConcurrentHashMap.newKeySet();
    final Set<VulkanImageAttachment> colorAttachments = ConcurrentHashMap.newKeySet();
    final Set<VulkanImageAttachment> depthAttachments = ConcurrentHashMap.newKeySet();
    final Set<VulkanUniformBuffer> framelyUpdatedUniforms = ConcurrentHashMap.newKeySet();
    final Set<VulkanUniformBuffer> manuallyUpdatedUniforms = ConcurrentHashMap.newKeySet();
    final AtomicBoolean uniformManuallyUpdated = new AtomicBoolean(false);
    final Set<CombinedImageSampler> textures = ConcurrentHashMap.newKeySet();
    final ConcurrentHashMap<VulkanDescriptorSetLayout, VkDescriptorPool> descriptorPools = new ConcurrentHashMap<>();
    final Set<VulkanDescriptorSet> descriptorSets = ConcurrentHashMap.newKeySet();

    final Set<VulkanRenderPass> renderPasses = new ConcurrentSkipListSet<>();
    final AtomicBoolean renderPassesNeedRecalculation = new AtomicBoolean(false);
    final List<CompiledRenderPassOp> compiledRenderPassOps = Collections.emptyList();

    final VulkanSwapchainAttachment swapchainColorAttachment = new VulkanSwapchainAttachment(new AttachmentCreateInfo(PixelFormat.RGBA8888_FLOAT, -1, -1));
    final VulkanSwapchainAttachment swapchainDepthAttachment = new VulkanSwapchainAttachment(new AttachmentCreateInfo(PixelFormat.DEPTH_BUFFER_OPTIMAL, -1, -1));

    private static final Logger logger = Logger.getLogger(VulkanRenderEngine.class.getName());
}
