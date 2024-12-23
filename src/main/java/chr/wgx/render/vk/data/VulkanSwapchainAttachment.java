package chr.wgx.render.vk.data;

import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.Resource;
import tech.icey.xjbutil.container.Option;

public final class VulkanSwapchainAttachment extends VulkanAttachment {
    public Resource.SwapchainImage[] swapchainImages;
    public Option<Resource.Image> msaaColorImage;

    public VulkanSwapchainAttachment(
            AttachmentCreateInfo createInfo,
            Resource.SwapchainImage[] swapchainImages,
            Option<Resource.Image> msaaColorImage
    ) {
        super(createInfo);
        this.swapchainImages = swapchainImages;
        this.msaaColorImage = msaaColorImage;
    }

    public void updateSwapchainImages(
            Resource.SwapchainImage[] swapchainImages,
            Option<Resource.Image> msaaColorImage
    ) {
        this.swapchainImages = swapchainImages;
        this.msaaColorImage = msaaColorImage;
    }
}
