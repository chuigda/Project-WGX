package chr.wgx.render.gles2.data;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.info.AttachmentCreateInfo;

public final class GLES2TextureAttachment extends Attachment {
    public final int textureObject;

    public GLES2TextureAttachment(AttachmentCreateInfo createInfo, int textureObject) {
        super(createInfo);
        this.textureObject = textureObject;
    }
}
