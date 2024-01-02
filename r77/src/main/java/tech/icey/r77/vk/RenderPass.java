package tech.icey.r77.vk;

public final class RenderPass {
    public RenderPass(Swapchain swapchain) {
        this.swapchain = swapchain;
    }

    public final Swapchain swapchain;
    public final long vkRenderPass = 0; // TODO(+)
}
