package chr.wgx.render.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public abstract class AbstractPipelineBindPoint {
    public final int priority;

    public final RenderPipeline pipeline;
    public final List<Attachment> colorAttachments;
    public final Option<Attachment> depthAttachment;

    protected AbstractPipelineBindPoint(
            int priority,
            RenderPipeline pipeline,
            List<Attachment> colorAttachments,
            Option<Attachment> depthAttachment
    ) {
        this.priority = priority;
        this.pipeline = pipeline;
        this.colorAttachments = colorAttachments;
        this.depthAttachment = depthAttachment;
    }
}
