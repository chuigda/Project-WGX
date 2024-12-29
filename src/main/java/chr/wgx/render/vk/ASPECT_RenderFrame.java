package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.vk.compiled.CompiledRenderPassOp;
import chr.wgx.render.vk.data.VulkanUniformBuffer;
import tech.icey.panama.NativeLayout;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkCommandBufferUsageFlags;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.VkCommandBufferBeginInfo;
import tech.icey.vk4j.datatype.VkPresentInfoKHR;
import tech.icey.vk4j.datatype.VkSubmitInfo;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkCommandBuffer;
import tech.icey.vk4j.handle.VkFence;
import tech.icey.vk4j.handle.VkSemaphore;
import tech.icey.vk4j.handle.VkSwapchainKHR;

@SuppressWarnings("FieldCanBeLocal")
public final class ASPECT_RenderFrame {
    ASPECT_RenderFrame(VulkanRenderEngine engine) {
        this.engine = engine;

        VulkanRenderEngineContext cx = engine.cx;
        this.pInFlightFences = VkFence.Buffer.allocate(cx.prefabArena, cx.inFlightFences.length);
        this.pImageIndex = IntBuffer.allocate(cx.prefabArena);
        this.pImageAvailableSemaphores = VkSemaphore.Buffer.allocate(
                cx.prefabArena,
                cx.imageAvailableSemaphores.length
        );
        this.pRenderFinishedSemaphores = VkSemaphore.Buffer.allocate(
                cx.prefabArena,
                cx.renderFinishedSemaphores.length
        );
        this.pWaitStages = IntBuffer.allocate(cx.prefabArena);
        this.commandBufferBeginInfos = VkCommandBufferBeginInfo.allocate(cx.prefabArena, cx.commandBuffers.length);
        this.pCommandBuffers = VkCommandBuffer.Buffer.allocate(cx.prefabArena, cx.commandBuffers.length);
        this.submitInfos = VkSubmitInfo.allocate(cx.prefabArena, cx.commandBuffers.length);
        this.pSwapchain = VkSwapchainKHR.Buffer.allocate(cx.prefabArena);
        this.presentInfos = VkPresentInfoKHR.allocate(cx.prefabArena, cx.commandBuffers.length);

        for (int i = 0; i < cx.inFlightFences.length; i++) {
            pInFlightFences.write(i, cx.inFlightFences[i]);
        }

        for (int i = 0; i < cx.imageAvailableSemaphores.length; i++) {
            pImageAvailableSemaphores.write(i, cx.imageAvailableSemaphores[i]);
        }

        for (int i = 0; i < cx.renderFinishedSemaphores.length; i++) {
            pRenderFinishedSemaphores.write(i, cx.renderFinishedSemaphores[i]);
        }

        pWaitStages.write(VkPipelineStageFlags.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

        for (int i = 0; i < cx.commandBuffers.length; i++) {
            commandBufferBeginInfos[i].flags(VkCommandBufferUsageFlags.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            pCommandBuffers.write(i, cx.commandBuffers[i]);

            submitInfos[i].waitSemaphoreCount(1);
            submitInfos[i].pWaitSemaphores(pImageAvailableSemaphores.offset(i));
            submitInfos[i].pWaitDstStageMask(pWaitStages);
            submitInfos[i].commandBufferCount(1);
            submitInfos[i].pCommandBuffers(pCommandBuffers.offset(i));
            submitInfos[i].signalSemaphoreCount(1);
            submitInfos[i].pSignalSemaphores(pRenderFinishedSemaphores.offset(i));

            presentInfos[i].waitSemaphoreCount(1);
            presentInfos[i].pWaitSemaphores(pRenderFinishedSemaphores.offset(i));
            presentInfos[i].swapchainCount(1);
            presentInfos[i].pSwapchains(pSwapchain);
            presentInfos[i].pImageIndices(pImageIndex);
        }
    }

    void renderFrameImpl(int currentFrameIndex) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;
        Swapchain swapchain = engine.swapchain;

        cx.dCmd.vkWaitForFences(
                cx.device,
                1,
                pInFlightFences.offset(currentFrameIndex),
                Constants.VK_TRUE,
                NativeLayout.UINT64_MAX
        );

        for (VulkanUniformBuffer uniform : engine.perFrameUpdatedUniforms) {
            uniform.updateGPU(currentFrameIndex);
        }

        @enumtype(VkResult.class) int result = cx.dCmd.vkAcquireNextImageKHR(
                cx.device,
                swapchain.vkSwapchain,
                NativeLayout.UINT64_MAX,
                cx.imageAvailableSemaphores[currentFrameIndex],
                null,
                pImageIndex
        );
        if (result == VkResult.VK_ERROR_OUT_OF_DATE_KHR) {
            return;
        }
        if (result != VkResult.VK_SUCCESS && result != VkResult.VK_SUBOPTIMAL_KHR) {
            throw new RenderException("无法获取交换链图像, 错误代码: " + VkResult.explain(result));
        }
        cx.dCmd.vkResetFences(cx.device, 1, pInFlightFences.offset(currentFrameIndex));

        int imageIndex = pImageIndex.read();
        engine.swapchainColorAttachment.swapchainImage = swapchain.swapchainImages[imageIndex];

        VkCommandBuffer cmdBuf = cx.commandBuffers[currentFrameIndex];

        cx.dCmd.vkResetCommandBuffer(cmdBuf, 0);
        result = cx.dCmd.vkBeginCommandBuffer(cmdBuf, commandBufferBeginInfos[currentFrameIndex]);
        if (result != VkResult.VK_SUCCESS) {
            throw new RenderException("无法开始记录指令缓冲, 错误代码: " + VkResult.explain(result));
        }

        for (CompiledRenderPassOp op : engine.compiledRenderPassOps) {
            op.recordToCommandBuffer(cx, swapchain, cmdBuf, currentFrameIndex);
        }

        result = cx.dCmd.vkEndCommandBuffer(cmdBuf);
        if (result != VkResult.VK_SUCCESS) {
            throw new RenderException("无法结束指令缓冲记录, 错误代码: " + VkResult.explain(result));
        }

        VkSubmitInfo submitInfo = submitInfos[currentFrameIndex];
        synchronized (cx.graphicsQueue) {
            result = cx.dCmd.vkQueueSubmit(cx.graphicsQueue, 1, submitInfo, cx.inFlightFences[currentFrameIndex]);
        }
        if (result != VkResult.VK_SUCCESS) {
            throw new RenderException("无法提交指令缓冲, 错误代码: " + VkResult.explain(result));
        }

        pSwapchain.write(swapchain.vkSwapchain);
        synchronized (
                cx.graphicsQueue.segment().address() == cx.presentQueue.segment().address() ?
                        cx.graphicsQueue :
                        cx.presentQueue
        ) {
            result = cx.dCmd.vkQueuePresentKHR(cx.presentQueue, presentInfos[currentFrameIndex]);
            if (result == VkResult.VK_ERROR_OUT_OF_DATE_KHR || result == VkResult.VK_SUBOPTIMAL_KHR) {
                return;
            }

            if (result != VkResult.VK_SUCCESS) {
                throw new RenderException("无法提交交换链图像, 错误代码: " + VkResult.explain(result));
            }
        }
    }

    private final VulkanRenderEngine engine;

    private final VkFence.Buffer pInFlightFences;
    private final IntBuffer pImageIndex;
    private final VkSemaphore.Buffer pImageAvailableSemaphores;
    private final VkSemaphore.Buffer pRenderFinishedSemaphores;
    private final IntBuffer pWaitStages;
    private final VkCommandBufferBeginInfo[] commandBufferBeginInfos;
    private final VkCommandBuffer.Buffer pCommandBuffers;
    private final VkSubmitInfo[] submitInfos;
    private final VkSwapchainKHR.Buffer pSwapchain;
    private final VkPresentInfoKHR[] presentInfos;
}
