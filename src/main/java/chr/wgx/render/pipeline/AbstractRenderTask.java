package chr.wgx.render.pipeline;

import chr.wgx.render.data.DescriptorSet;
import chr.wgx.render.data.RenderObject;

import java.util.HashMap;

public abstract class AbstractRenderTask {
    public final RenderObject renderObject;
    public final HashMap<Integer, DescriptorSet> descriptorSets;
    // TODO push constant

    protected AbstractRenderTask(
            RenderObject renderObject,
            HashMap<Integer, DescriptorSet> descriptorSets
    ) {
        this.renderObject = renderObject;
        this.descriptorSets = descriptorSets;
    }
}
