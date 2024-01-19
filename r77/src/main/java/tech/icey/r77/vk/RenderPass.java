package tech.icey.r77.vk;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import tech.icey.util.ManualDispose;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.runtimeError;

public final class RenderPass implements ManualDispose {
    public RenderPass(Swapchain swapchain) {
        this.swapchain = swapchain;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1, stack);
            attachments.get(0)
                    .format(swapchain.surfaceFormat().imageFormat)
                    .samples(VK_SAMPLE_COUNT_1_BIT)
                    .loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR)
                    .storeOp(VK_ATTACHMENT_STORE_OP_STORE)
                    .initialLayout(VK_IMAGE_LAYOUT_UNDEFINED)
                    .finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(1, stack)
                    .attachment(0)
                    .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subPass = VkSubpassDescription.calloc(1, stack)
                    .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                    .colorAttachmentCount(colorReference.remaining())
                    .pColorAttachments(colorReference);

            VkRenderPassCreateInfo renderPassCreateInfo = VkRenderPassCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                    .pAttachments(attachments)
                    .pSubpasses(subPass)
                    .pDependencies(subpassDependencies);

            LongBuffer renderPassBuffer = stack.mallocLong(1);
            int ret = vkCreateRenderPass(swapchain.device().vkDevice(), renderPassCreateInfo, null, renderPassBuffer);
            if (ret != VK_SUCCESS) {
                runtimeError("无法创建 RenderPass");
            }
        }
    }

    public final Swapchain swapchain;
    public final long vkRenderPass = 0;

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
        vkDestroyRenderPass(swapchain.device().vkDevice(), vkRenderPass, null);
    }

    private boolean isDisposed = false;
}
