package chr.wgx.render.task;

import chr.wgx.render.data.Attachment;
import chr.wgx.render.data.RenderPipeline;
import chr.wgx.render.info.RenderPassCreateInfo;

import java.util.List;

public abstract class RenderPass implements Comparable<RenderPass> {
    public final RenderPassCreateInfo info;

    protected RenderPass(RenderPassCreateInfo info) {
        this.info = info;
    }

    public abstract void addInputAttachments(List<Attachment> attachments);

    public abstract RenderPipelineBind createPipelineBind(int priority, RenderPipeline pipeline);

    @Override
    public final int compareTo(RenderPass o) {
        return Integer.compare(info.priority, o.info.priority);
    }
}
