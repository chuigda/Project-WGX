package chr.wgx.render.data;

import chr.wgx.render.info.AttachmentCreateInfo;

public abstract class Attachment {
    public final AttachmentCreateInfo createInfo;

    public Attachment(AttachmentCreateInfo createInfo) {
        this.createInfo = createInfo;
    }
}
