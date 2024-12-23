package chr.wgx.render.vk.data;

import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.Resource;
import tech.icey.xjbutil.container.Option;

public final class VulkanSwapchainAttachment extends VulkanAttachment {
    public Resource.SwapchainImage swapchainImage;
    public Option<Resource.Image> msaaColorImage;

    public VulkanSwapchainAttachment(
            AttachmentCreateInfo createInfo,
            Resource.SwapchainImage swapchainImage,
            Option<Resource.Image> msaaColorImage
    ) {
        super(createInfo);

        this.swapchainImage = swapchainImage;
        this.msaaColorImage = msaaColorImage;
    }
}
