package chr.wgx.render.data;

import chr.wgx.render.info.UniformBufferCreateInfo;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;

public abstract non-sealed class UniformBuffer extends Descriptor {
    public final UniformBufferCreateInfo createInfo;

    public UniformBuffer(UniformBufferCreateInfo createInfo) {
        this.createInfo = createInfo;
    }

    public abstract void updateBufferContent(MemorySegment segment);
    public abstract void updateBufferContent(Action1<MemorySegment> updateAction);
}
