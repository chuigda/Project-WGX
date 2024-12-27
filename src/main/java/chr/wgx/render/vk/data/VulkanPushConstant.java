package chr.wgx.render.vk.data;

import chr.wgx.render.data.PushConstant;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;

public final class VulkanPushConstant extends PushConstant {
    public final MemorySegment segment;

    public VulkanPushConstant(MemorySegment segment) {
        this.segment = segment;
    }

    @Override
    public synchronized void updateBufferContent(MemorySegment segment) {
        this.segment.copyFrom(segment);
    }

    @Override
    public synchronized void updateBufferContent(Action1<MemorySegment> updateAction) {
        updateAction.apply(this.segment);
    }
}
