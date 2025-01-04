package chr.wgx.render.data;

import chr.wgx.render.info.PushConstantInfo;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;

public abstract class PushConstant {
    public final PushConstantInfo createInfo;

    protected PushConstant(PushConstantInfo createInfo) {
        this.createInfo = createInfo;
    }

    public abstract void updateBufferContent(MemorySegment segment);
    public abstract void updateBufferContent(Action1<MemorySegment> updateAction);
}
