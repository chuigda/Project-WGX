package chr.wgx.render.gles2.data;

import chr.wgx.render.data.UniformBuffer;
import chr.wgx.render.info.UniformBufferCreateInfo;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.MemorySegment;

public final class GLES2UniformBuffer extends UniformBuffer {
    public final MemorySegment cpuBuffer;

    public GLES2UniformBuffer(UniformBufferCreateInfo createInfo, MemorySegment cpuBuffer) {
        super(createInfo);
        this.cpuBuffer = cpuBuffer;
    }

    @Override
    public void updateBufferContent(MemorySegment segment) {
        synchronized (cpuBuffer) {
            cpuBuffer.copyFrom(segment);
        }
    }

    @Override
    public void updateBufferContent(Action1<MemorySegment> updateAction) {
        synchronized (cpuBuffer) {
            updateAction.apply(cpuBuffer);
        }
    }
}
