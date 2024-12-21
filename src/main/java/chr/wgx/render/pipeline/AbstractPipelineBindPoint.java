package chr.wgx.render.pipeline;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import tech.icey.xjbutil.container.Option;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPipelineBindPoint {
    public final RenderPipeline pipeline;
    public final List<Attachment> colorAttachments;
    public final Option<Attachment> depthAttachment;

    public final List<AbstractRenderTaskGroup> objectGroups = new ArrayList<>();

    protected AbstractPipelineBindPoint(
            RenderPipeline pipeline,
            List<Attachment> colorAttachments,
            Option<Attachment> depthAttachment
    ) {
        this.pipeline = pipeline;
        this.colorAttachments = colorAttachments;
        this.depthAttachment = depthAttachment;
    }
}
