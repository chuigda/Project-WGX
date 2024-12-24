package chr.wgx.render.info;

import java.lang.foreign.MemorySegment;

@SuppressWarnings("ClassCanBeRecord")
public final class ObjectCreateInfo {
    public final VertexInputInfo vertexInputInfo;
    public final MemorySegment pVertices;
    public final MemorySegment pIndices;

    public ObjectCreateInfo(VertexInputInfo vertexInputInfo, MemorySegment pVertices, MemorySegment pIndices) {
        assert pVertices.byteSize() % vertexInputInfo.stride == 0 && pIndices.byteSize() % Integer.BYTES == 0;

        this.vertexInputInfo = vertexInputInfo;
        this.pVertices = pVertices;
        this.pIndices = pIndices;
    }
}
