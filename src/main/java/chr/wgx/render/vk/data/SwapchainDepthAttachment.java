package chr.wgx.render.vk.data;

import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.info.AttachmentCreateInfo;

public class SwapchainDepthAttachment extends VulkanAttachment {
    public SwapchainDepthAttachment() {
        super(new AttachmentCreateInfo(PixelFormat.DEPTH_BUFFER_OPTIMAL, -1, -1));
    }
}
