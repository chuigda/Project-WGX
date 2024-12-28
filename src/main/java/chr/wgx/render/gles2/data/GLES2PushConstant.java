package chr.wgx.render.gles2.data;

import chr.wgx.render.data.PushConstant;
import chr.wgx.render.info.PushConstantInfo;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;

public final class GLES2PushConstant extends PushConstant {
    public final MemorySegment cpuBuffer;

    public GLES2PushConstant(PushConstantInfo info, MemorySegment cpuBuffer) {
        super(info);
        this.cpuBuffer = cpuBuffer;
    }

    @Override
    public synchronized void updateBufferContent(MemorySegment segment) {
        cpuBuffer.copyFrom(segment);
    }

    @Override
    public synchronized void updateBufferContent(Action1<MemorySegment> updateAction) {
        updateAction.apply(cpuBuffer);
    }
}
