package chr.wgx.render.gles2.data;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.info.AttachmentCreateInfo;

public class GLES2Attachment extends Attachment {
    public final int fbo;

    public GLES2Attachment(AttachmentCreateInfo createInfo, int fbo) {
        super(createInfo);
        this.fbo = fbo;
    }
}
