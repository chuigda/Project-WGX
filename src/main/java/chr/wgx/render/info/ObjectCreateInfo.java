package chr.wgx.render.info;

import tech.icey.panama.IPointer;

@SuppressWarnings("ClassCanBeRecord")
public final class ObjectCreateInfo {
    public final VertexInputInfo vertexInputInfo;
    public final IPointer pData;

    public ObjectCreateInfo(VertexInputInfo vertexInputInfo, IPointer pData) {
        this.vertexInputInfo = vertexInputInfo;
        this.pData = pData;
    }
}
