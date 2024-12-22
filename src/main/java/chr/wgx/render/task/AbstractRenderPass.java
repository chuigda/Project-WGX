package chr.wgx.render.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public abstract class AbstractRenderPass {
    public final String renderPassName;
    public final int priority;

    protected AbstractRenderPass(
            String renderPassName,
            int priority,
            List<Attachment> inputAttachments,
            List<Attachment> outputAttachments
    ) {
        this.renderPassName = renderPassName;
        this.priority = priority;

        this.addInputAttachment(inputAttachments);
        this.addOutputAttachment(outputAttachments);
    }

    public abstract void addInputAttachment(Attachment... attachments);
    public abstract void addOutputAttachment(Attachment... attachments);
    public abstract void addInputAttachment(List<Attachment> attachments);
    public abstract void addOutputAttachment(List<Attachment> attachments);

    public abstract AbstractPipelineBindPoint addPipelineBindPoint(
            int priority,
            RenderPipeline pipeline,
            List<Attachment> colorAttachments,
            Option<Attachment> depthAttachment
    );
}
