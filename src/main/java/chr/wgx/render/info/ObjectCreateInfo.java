package chr.wgx.render.info;

import java.lang.foreign.MemorySegment;

@SuppressWarnings("ClassCanBeRecord")
public final class ObjectCreateInfo {
    public final VertexInputInfo vertexInputInfo;
    public final MemorySegment pData;

    public ObjectCreateInfo(VertexInputInfo vertexInputInfo, MemorySegment pData) {
        assert pData.byteSize() % vertexInputInfo.stride == 0;

        this.vertexInputInfo = vertexInputInfo;
        this.pData = pData;
    }
}
