package chr.wgx.render.common;

import java.lang.foreign.MemorySegment;

public interface IUniformBufferObjectCPU {
    void writeToBuffer(MemorySegment segment);
    void writeComponentToBuffer(int component, MemorySegment segment);

    boolean isDirty();
    boolean clearDirty();
}
