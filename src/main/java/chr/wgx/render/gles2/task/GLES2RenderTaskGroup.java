package chr.wgx.render.gles2.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.PushConstant;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.gles2.data.GLES2DescriptorSet;
import chr.wgx.render.gles2.data.GLES2PushConstant;
import chr.wgx.render.gles2.data.GLES2RenderObject;
import chr.wgx.render.task.RenderTask;
import chr.wgx.render.task.RenderTaskDynamic;
import chr.wgx.render.task.RenderTaskGroup;
import tech.icey.xjbutil.container.Option;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class GLES2RenderTaskGroup extends RenderTaskGroup {
    public final List<GLES2DescriptorSet> sharedDescriptorSets;
    public final ConcurrentLinkedQueue<GLES2RenderTask> renderTasks = new ConcurrentLinkedQueue<>();
    public final ConcurrentLinkedQueue<GLES2RenderTaskDynamic> dynamicRenderTasks = new ConcurrentLinkedQueue<>();

    GLES2RenderTaskGroup(List<DescriptorSet> sharedDescriptorSets) {
        this.sharedDescriptorSets = sharedDescriptorSets.stream()
                .map(ds -> (GLES2DescriptorSet) ds)
                .toList();
    }

    @Override
    public RenderTask addRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant
    ) {
        GLES2RenderTask ret = new GLES2RenderTask(
                (GLES2RenderObject) renderObject,
                descriptorSets.stream().map(ds -> (GLES2DescriptorSet) ds).toList(),
                pushConstant.map(pc -> (GLES2PushConstant) pc)
        );
        renderTasks.add(ret);
        return ret;
    }

    @Override
    public RenderTaskDynamic addDynamicRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant
    ) {
        return null;
    }
}
