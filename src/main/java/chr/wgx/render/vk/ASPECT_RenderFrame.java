package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.vk.compiled.CompiledRenderPassOp;
import chr.wgx.render.vk.data.VulkanUniformBuffer;
import club.doki7.ffm.NativeLayout;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkCommandBufferUsageFlags;
import club.doki7.vulkan.bitmask.VkPipelineStageFlags;
import club.doki7.vulkan.datatype.VkCommandBufferBeginInfo;
import club.doki7.vulkan.datatype.VkPresentInfoKHR;
import club.doki7.vulkan.datatype.VkSubmitInfo;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vulkan.handle.VkCommandBuffer;
import club.doki7.vulkan.handle.VkFence;
import club.doki7.vulkan.handle.VkSemaphore;
import club.doki7.vulkan.handle.VkSwapchainKHR;

@SuppressWarnings("FieldCanBeLocal")
public final class ASPECT_RenderFrame {
    ASPECT_RenderFrame(VulkanRenderEngine engine) {
        this.engine = engine;

        VulkanRenderEngineContext cx = engine.cx;
        this.pInFlightFences = VkFence.Ptr.allocate(cx.prefabArena, cx.inFlightFences.length);
        this.pImageIndex = IntPtr.allocate(cx.prefabArena);
        this.pImageAvailableSemaphores = VkSemaphore.Ptr.allocate(
                cx.prefabArena,
                cx.imageAvailableSemaphores.length
        );
        this.pRenderFinishedSemaphores = VkSemaphore.Ptr.allocate(
                cx.prefabArena,
                cx.renderFinishedSemaphores.length
        );
        this.pWaitStages = IntPtr.allocate(cx.prefabArena);
        this.commandBufferBeginInfos = VkCommandBufferBeginInfo.allocate(cx.prefabArena, cx.commandBuffers.length);
        this.pCommandBuffers = VkCommandBuffer.Ptr.allocate(cx.prefabArena, cx.commandBuffers.length);
        this.submitInfos = VkSubmitInfo.allocate(cx.prefabArena, cx.commandBuffers.length);
        this.pSwapchain = VkSwapchainKHR.Ptr.allocate(cx.prefabArena);
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

        pWaitStages.write(VkPipelineStageFlags.COLOR_ATTACHMENT_OUTPUT);

        for (int i = 0; i < cx.commandBuffers.length; i++) {
            commandBufferBeginInfos.at(i).flags(VkCommandBufferUsageFlags.ONE_TIME_SUBMIT);
            pCommandBuffers.write(i, cx.commandBuffers[i]);

            VkSubmitInfo submitInfo = submitInfos.at(i);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(pImageAvailableSemaphores.offset(i));
            submitInfo.pWaitDstStageMask(pWaitStages);
            submitInfo.commandBufferCount(1);
            submitInfo.pCommandBuffers(pCommandBuffers.offset(i));
            submitInfo.signalSemaphoreCount(1);
            submitInfo.pSignalSemaphores(pRenderFinishedSemaphores.offset(i));

            VkPresentInfoKHR presentInfo = presentInfos.at(i);
            presentInfo.waitSemaphoreCount(1);
            presentInfo.pWaitSemaphores(pRenderFinishedSemaphores.offset(i));
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(pSwapchain);
            presentInfo.pImageIndices(pImageIndex);
        }
    }

    void renderFrameImpl(int currentFrameIndex) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;
        Swapchain swapchain = engine.swapchain;

        cx.dCmd.waitForFences(
                cx.device,
                1,
                pInFlightFences.offset(currentFrameIndex),
                VkConstants.TRUE,
                NativeLayout.UINT64_MAX
        );

        for (VulkanUniformBuffer uniform : engine.perFrameUpdatedUniforms) {
            uniform.updateGPU(currentFrameIndex);
        }

        @EnumType(VkResult.class) int result = cx.dCmd.acquireNextImageKHR(
                cx.device,
                swapchain.vkSwapchain,
                NativeLayout.UINT64_MAX,
                cx.imageAvailableSemaphores[currentFrameIndex],
                null,
                pImageIndex
        );
        if (result == VkResult.ERROR_OUT_OF_DATE_KHR) {
            return;
        }
        if (result != VkResult.SUCCESS && result != VkResult.SUBOPTIMAL_KHR) {
            throw new RenderException("无法获取交换链图像, 错误代码: " + VkResult.explain(result));
        }
        cx.dCmd.resetFences(cx.device, 1, pInFlightFences.offset(currentFrameIndex));

        int imageIndex = pImageIndex.read();
        engine.swapchainColorAttachment.swapchainImage = swapchain.swapchainImages[imageIndex];

        VkCommandBuffer cmdBuf = cx.commandBuffers[currentFrameIndex];

        cx.dCmd.resetCommandBuffer(cmdBuf, 0);
        result = cx.dCmd.beginCommandBuffer(cmdBuf, commandBufferBeginInfos.at(currentFrameIndex));
        if (result != VkResult.SUCCESS) {
            throw new RenderException("无法开始记录指令缓冲, 错误代码: " + VkResult.explain(result));
        }

        for (CompiledRenderPassOp op : engine.compiledRenderPassOps) {
            op.recordToCommandBuffer(cx, swapchain, cmdBuf, currentFrameIndex);
        }

        result = cx.dCmd.endCommandBuffer(cmdBuf);
        if (result != VkResult.SUCCESS) {
            throw new RenderException("无法结束指令缓冲记录, 错误代码: " + VkResult.explain(result));
        }

        VkSubmitInfo submitInfo = submitInfos.at(currentFrameIndex);
        synchronized (cx.graphicsQueue) {
            result = cx.dCmd.queueSubmit(cx.graphicsQueue, 1, submitInfo, cx.inFlightFences[currentFrameIndex]);
        }
        if (result != VkResult.SUCCESS) {
            throw new RenderException("无法提交指令缓冲, 错误代码: " + VkResult.explain(result));
        }

        pSwapchain.write(swapchain.vkSwapchain);
        synchronized (
                cx.graphicsQueue.segment().address() == cx.presentQueue.segment().address() ?
                        cx.graphicsQueue :
                        cx.presentQueue
        ) {
            result = cx.dCmd.queuePresentKHR(cx.presentQueue, presentInfos.at(currentFrameIndex));
            if (result == VkResult.ERROR_OUT_OF_DATE_KHR || result == VkResult.SUBOPTIMAL_KHR) {
                return;
            }

            if (result != VkResult.SUCCESS) {
                throw new RenderException("无法提交交换链图像, 错误代码: " + VkResult.explain(result));
            }
        }
    }

    private final VulkanRenderEngine engine;

    private final VkFence.Ptr pInFlightFences;
    private final IntPtr pImageIndex;
    private final VkSemaphore.Ptr pImageAvailableSemaphores;
    private final VkSemaphore.Ptr pRenderFinishedSemaphores;
    private final IntPtr pWaitStages;
    private final VkCommandBufferBeginInfo.Ptr commandBufferBeginInfos;
    private final VkCommandBuffer.Ptr pCommandBuffers;
    private final VkSubmitInfo.Ptr submitInfos;
    private final VkSwapchainKHR.Ptr pSwapchain;
    private final VkPresentInfoKHR.Ptr presentInfos;
}
