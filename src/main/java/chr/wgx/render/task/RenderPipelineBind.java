package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;

import java.util.List;

public abstract class RenderPipelineBind implements Comparable<RenderPipelineBind> {
    public final int priority;

    protected RenderPipelineBind(int priority) {
        this.priority = priority;
    }

    public abstract RenderTaskGroup createRenderTaskGroup(List<DescriptorSet> sharedDescriptorSets);

    @Override
    public final int compareTo(RenderPipelineBind o) {
        return Integer.compare(priority, o.priority);
    }
}
