package chr.wgx.render.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;

import java.util.List;

public abstract class RenderPass implements Comparable<RenderPass> {
    public final String renderPassName;
    public final int priority;

    protected RenderPass(String renderPassName, int priority) {
        this.renderPassName = renderPassName;
        this.priority = priority;
    }

    public abstract void addInputAttachments(List<Attachment> attachments);

    public abstract RenderPipelineBind createPipelineBind(int priority, RenderPipeline pipeline);

    @Override
    public final int compareTo(RenderPass o) {
        return Integer.compare(priority, o.priority);
    }
}