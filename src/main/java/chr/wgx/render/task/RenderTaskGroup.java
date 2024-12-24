package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RenderTaskGroup {
    protected RenderTaskGroup() {}

    public abstract RenderTask addRenderTask(
            RenderObject renderObject,
            List<DescriptorSet> descriptorSets
            // TODO push constants
    );

    public final void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public final boolean isEnabled() {
        return this.enabled.get();
    }

    private final AtomicBoolean enabled = new AtomicBoolean(true);
}
