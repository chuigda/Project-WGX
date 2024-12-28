package chr.wgx.render.gles2.data;

import chr.wgx.render.data.UniformBuffer;
import chr.wgx.render.info.UniformBufferCreateInfo;
import tech.icey.gles2.GLES2;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;

public final class GLES2UniformBuffer extends UniformBuffer {
    public final MemorySegment cpuBuffer;

    public GLES2UniformBuffer(UniformBufferCreateInfo createInfo, MemorySegment cpuBuffer) {
        super(createInfo);
        this.cpuBuffer = cpuBuffer;
    }

    @Override
    public synchronized void updateBufferContent(MemorySegment segment) {
        cpuBuffer.copyFrom(segment);
    }

    @Override
    public void updateBufferContent(Action1<MemorySegment> updateAction) {
        updateAction.apply(cpuBuffer);
    }
}
