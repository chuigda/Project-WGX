package chr.wgx.render.info;

import java.lang.foreign.MemorySegment;

@SuppressWarnings("ClassCanBeRecord")
public final class ObjectCreateInfo {
    public final VertexInputInfo vertexInputInfo;
    public final MemorySegment pData;

    public ObjectCreateInfo(VertexInputInfo vertexInputInfo, MemorySegment pData) {
        this.vertexInputInfo = vertexInputInfo;
        this.pData = pData;
    }
}
