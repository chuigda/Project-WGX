package chr.wgx.render.gles2.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.gles2.data.GLES2RenderPipeline;
import chr.wgx.render.task.RenderPipelineBind;
import chr.wgx.render.task.RenderTaskGroup;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GLES2RenderPipelineBind extends RenderPipelineBind {
    public final GLES2RenderPipeline pipeline;
    public final ConcurrentLinkedQueue<GLES2RenderTaskGroup> renderTaskGroups = new ConcurrentLinkedQueue<>();

    public GLES2RenderPipelineBind(int priority, GLES2RenderPipeline pipeline) {
        super(priority);
        this.pipeline = pipeline;
    }

    @Override
    public RenderTaskGroup createRenderTaskGroup(List<DescriptorSet> sharedDescriptorSets) {
        GLES2RenderTaskGroup ret = new GLES2RenderTaskGroup(sharedDescriptorSets);
        renderTaskGroups.add(ret);
        return ret;
    }
}
