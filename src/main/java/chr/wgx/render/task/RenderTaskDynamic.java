package chr.wgx.render.task;

import chr.wgx.render.data.DescriptorSet;

public abstract class RenderTaskDynamic extends RenderTask {
    public abstract void updateDescriptorSet(int location, DescriptorSet descriptorSet);
}
