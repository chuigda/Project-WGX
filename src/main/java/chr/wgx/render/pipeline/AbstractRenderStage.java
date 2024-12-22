package chr.wgx.render.pipeline;

import chr.wgx.render.data.Attachment;

import java.util.List;

public abstract class AbstractRenderStage {
    public final int priority;
    public final List<Attachment> inputAttachments;
    public final List<Attachment> outputAttachments;

    protected AbstractRenderStage(
            int priority,
            List<Attachment> inputAttachments,
            List<Attachment> outputAttachments
    ) {
        this.priority = priority;

        this.inputAttachments = inputAttachments;
        this.outputAttachments = outputAttachments;
    }
}
