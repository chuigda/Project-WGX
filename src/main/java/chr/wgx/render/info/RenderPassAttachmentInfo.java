package chr.wgx.render.info;

import chr.wgx.render.common.ClearBehavior;
import chr.wgx.render.common.Color;
import chr.wgx.render.data.Attachment;

public final class RenderPassAttachmentInfo {
    public final Attachment attachment;
    public final ClearBehavior clearBehavior;
    public final Color clearColor;

    public RenderPassAttachmentInfo(Attachment attachment, ClearBehavior clearBehavior, Color clearColor) {
        this.attachment = attachment;
        this.clearBehavior = clearBehavior;
        this.clearColor = clearColor;
    }

    public RenderPassAttachmentInfo(Attachment attachment, ClearBehavior clearBehavior) {
        this(attachment, clearBehavior, HIDDEN_COLOR_UNUSED);
    }

    // purple or magenta color
    private static final Color HIDDEN_COLOR_UNUSED = new Color(1.0f, 0.0f, 1.0f, 1.0f);
}
