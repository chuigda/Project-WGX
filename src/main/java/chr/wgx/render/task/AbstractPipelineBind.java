package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;

import java.util.HashMap;

public abstract class AbstractPipelineBind implements Comparable<AbstractPipelineBind> {
    public final int priority;

    protected AbstractPipelineBind(int priority) {
        this.priority = priority;
    }

    public abstract AbstractRenderTaskGroup addRenderTaskGroup(HashMap<Integer, DescriptorSet> sharedDescriptorSets);

    @Override
    public int compareTo(AbstractPipelineBind o) {
        return Integer.compare(priority, o.priority);
    }
}
