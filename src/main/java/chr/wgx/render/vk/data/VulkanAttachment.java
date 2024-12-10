package chr.wgx.render.vk.data;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.info.AttachmentCreateInfo;

public abstract class VulkanAttachment extends Attachment {
    public VulkanAttachment(AttachmentCreateInfo createInfo) {
        super(createInfo);
    }
}
