package chr.wgx.render.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;

import java.util.List;

public abstract class AbstractRenderPass implements Comparable<AbstractRenderPass> {
    public final String renderPassName;
    public final int priority;

    protected AbstractRenderPass(String renderPassName, int priority) {
        this.renderPassName = renderPassName;
        this.priority = priority;
    }

    public abstract void addInputAttachments(List<Attachment> attachments);

    public abstract AbstractPipelineBind addPipelineBindPoint(int priority, RenderPipeline pipeline);

    @Override
    public int compareTo(AbstractRenderPass o) {
        return Integer.compare(priority, o.priority);
    }
}
