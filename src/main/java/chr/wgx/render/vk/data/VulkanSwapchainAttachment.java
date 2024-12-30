package chr.wgx.render.vk.data;

import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.Resource;

public final class VulkanSwapchainAttachment extends VulkanAttachment {
    public Resource.SwapchainImage swapchainImage;

    public VulkanSwapchainAttachment(AttachmentCreateInfo createInfo, Resource.SwapchainImage swapchainImage) {
        super(createInfo);

        this.swapchainImage = swapchainImage;
    }
}
