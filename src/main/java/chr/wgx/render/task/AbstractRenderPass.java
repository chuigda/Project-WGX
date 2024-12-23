package chr.wgx.render.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import tech.icey.xjbutil.container.Option;

import java.util.List;

public abstract class AbstractRenderPass implements Comparable<AbstractRenderPass> {
    public final String renderPassName;
    public final int priority;

    protected AbstractRenderPass(String renderPassName, int priority) {
        this.renderPassName = renderPassName;
        this.priority = priority;
    }

    public abstract void addAttachments(
            List<Attachment> inputAttachments,
            List<Attachment> outputAttachments
    );

    public abstract AbstractPipelineBindPoint addPipelineBindPoint(
            int priority,
            RenderPipeline pipeline,
            List<Attachment> colorAttachments,
            Option<Attachment> depthAttachment
    );

    @Override
    public int compareTo(AbstractRenderPass o) {
        return Integer.compare(priority, o.priority);
    }
}
