package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderPipeline;

import java.util.HashMap;

public abstract class AbstractPipelineBindPoint implements Comparable<AbstractPipelineBindPoint> {
    public final int priority;

    public final RenderPipeline pipeline;

    protected AbstractPipelineBindPoint(int priority, RenderPipeline pipeline) {
        this.priority = priority;
        this.pipeline = pipeline;
    }

    public abstract AbstractRenderTaskGroup addRenderTaskGroup(
            HashMap<Integer, DescriptorSet> sharedDescriptorSets
    );

    @Override
    public int compareTo(AbstractPipelineBindPoint o) {
        return Integer.compare(priority, o.priority);
    }
}
