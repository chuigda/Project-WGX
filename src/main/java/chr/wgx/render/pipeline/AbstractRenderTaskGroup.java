package chr.wgx.render.pipeline;

import chr.wgx.render.data.DescriptorSet;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractRenderTaskGroup {
    public final AtomicBoolean enabled;
    public final HashMap<Integer, DescriptorSet> sharedDescriptorSets;

    protected AbstractRenderTaskGroup(
            AtomicBoolean enabled,
            HashMap<Integer, DescriptorSet> sharedDescriptorSets
    ) {
        this.enabled = enabled;
        this.sharedDescriptorSets = sharedDescriptorSets;
    }
}
