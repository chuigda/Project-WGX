package chr.wgx.render.data;

import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;

public abstract class PushConstant {
    public abstract void updateBufferContent(MemorySegment segment);
    public abstract void updateBufferContent(Action1<MemorySegment> updateAction);
}
