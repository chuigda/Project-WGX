package chr.wgx.render.pipeline;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderObject;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractRenderTask {
    public final RenderObject renderObject;
    public final HashMap<Integer, DescriptorSet> descriptorSets;
    // TODO push constant

    public final AtomicBoolean enabled = new AtomicBoolean(true);

    protected AbstractRenderTask(
            RenderObject renderObject,
            HashMap<Integer, DescriptorSet> descriptorSets
    ) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public final boolean isEnabled() {
        return this.enabled.get();
    }
}
