package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.PushConstant;
import chr.wgx.render.data.RenderObject;
import tech.icey.xjbutil.container.Option;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RenderTaskGroup {
    protected RenderTaskGroup() {}

    public abstract RenderTask addRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant
    );

    public abstract RenderTaskDynamic addDynamicRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            Option<PushConstant> pushConstant
    );

    public final RenderTask addRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            PushConstant pushConstant
    ) {
        return this.addRenderTask(renderObject, descriptorSets, Option.some(pushConstant));
    }

    public final RenderTask addRenderTask(RenderObject renderObject, List<DescriptorSet> descriptorSets) {
        return this.addRenderTask(renderObject, descriptorSets, Option.none());
    }

    public final RenderTask addRenderTask(RenderObject renderObject) {
        return this.addRenderTask(renderObject, List.of(), Option.none());
    }

    public final RenderTaskDynamic addDynamicRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets,
            PushConstant pushConstant
    ) {
        return this.addDynamicRenderTask(renderObject, descriptorSets, Option.some(pushConstant));
    }

    public final RenderTaskDynamic addDynamicRenderTask(RenderObject renderObject, List<DescriptorSet> descriptorSets) {
        return this.addDynamicRenderTask(renderObject, descriptorSets, Option.none());
    }

    public final RenderTaskDynamic addDynamicRenderTask(RenderObject renderObject) {
        return this.addDynamicRenderTask(renderObject, List.of(), Option.none());
    }

    public final void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public final boolean isEnabled() {
        return this.enabled.get();
    }

    private final AtomicBoolean enabled = new AtomicBoolean(true);
}
