package chr.wgx.render.vk.data;

import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.Resource;

public final class VulkanSwapchainAttachment extends VulkanAttachment {
    public Resource.SwapchainImage[] swapchainImages;

    public VulkanSwapchainAttachment(
            AttachmentCreateInfo createInfo,
            Resource.SwapchainImage[] swapchainImages
    ) {
        super(createInfo);
        this.swapchainImages = swapchainImages;
    }

    public void updateSwapchainImages(Resource.SwapchainImage[] swapchainImages) {
        this.swapchainImages = swapchainImages;
    }
}
