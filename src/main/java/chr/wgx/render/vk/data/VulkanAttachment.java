package chr.wgx.render.vk.data;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.info.AttachmentCreateInfo;

public abstract sealed class VulkanAttachment extends Attachment permits
        VulkanImageAttachment,
        VulkanSwapchainAttachment
{
    protected VulkanAttachment(AttachmentCreateInfo createInfo) {
        super(createInfo);
    }
}
