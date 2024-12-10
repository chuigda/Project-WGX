package chr.wgx.render.vk.data;

import chr.wgx.render.common.PixelFormat;
import chr.wgx.render.info.AttachmentCreateInfo;

public final class SwapchainColorAttachment extends VulkanAttachment {
    public SwapchainColorAttachment() {
        super(new AttachmentCreateInfo(PixelFormat.RGBA8888_FLOAT, -1, -1));
    }
}
