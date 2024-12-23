package chr.wgx.render.vk.data;

import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.Resource;
import tech.icey.xjbutil.container.Option;

public final class VulkanSwapchainAttachment extends VulkanAttachment {
    public final Option<Resource.SwapchainImage[]> swapchainImages;

    public VulkanSwapchainAttachment(AttachmentCreateInfo createInfo) {
        super(createInfo);
        this.swapchainImages = Option.none();
    }
}
