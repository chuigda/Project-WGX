package chr.wgx.render.vk.data;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.info.AttachmentCreateInfo;
import chr.wgx.render.vk.Resource;

public final class VkAttachment extends Attachment {
    public final Resource.Image image;

    public VkAttachment(AttachmentCreateInfo createInfo, Resource.Image image) {
        super(createInfo);
        this.image = image;
    }
}
