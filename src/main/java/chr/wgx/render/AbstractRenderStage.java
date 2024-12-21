package chr.wgx.render;

import chr.wgx.render.data.Attachment;

import java.util.List;

public abstract class AbstractRenderStage {
    public final List<Attachment> inputAttachments;
    public final List<Attachment> outputAttachments;

    protected AbstractRenderStage(List<Attachment> inputAttachments, List<Attachment> outputAttachments) {
        this.inputAttachments = inputAttachments;
        this.outputAttachments = outputAttachments;
    }
}
