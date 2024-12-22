package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderObject;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractRenderTaskGroup {
    public final HashMap<Integer, DescriptorSet> sharedDescriptorSets;

    protected AbstractRenderTaskGroup(HashMap<Integer, DescriptorSet> sharedDescriptorSets) {
        this.sharedDescriptorSets = sharedDescriptorSets;
    }

    public abstract AbstractRenderTask addRenderTask(
            RenderObject renderObject,
            HashMap<Integer, DescriptorSet> descriptorSets
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
