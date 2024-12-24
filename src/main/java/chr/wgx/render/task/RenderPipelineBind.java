package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;

import java.util.HashMap;

public abstract class RenderPipelineBind implements Comparable<RenderPipelineBind> {
    public final int priority;

    protected RenderPipelineBind(int priority) {
        this.priority = priority;
    }

    public abstract RenderTaskGroup addRenderTaskGroup(HashMap<Integer, DescriptorSet> sharedDescriptorSets);

    @Override
    public final int compareTo(RenderPipelineBind o) {
        return Integer.compare(priority, o.priority);
    }
}
