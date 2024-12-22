package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractRenderTaskGroup {
    public final HashMap<Integer, DescriptorSet> sharedDescriptorSets;

    public final AtomicBoolean enabled = new AtomicBoolean(true);

    protected AbstractRenderTaskGroup(HashMap<Integer, DescriptorSet> sharedDescriptorSets) {
        this.sharedDescriptorSets = sharedDescriptorSets;
    }

    public final void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public final boolean isEnabled() {
        return this.enabled.get();
    }
}
