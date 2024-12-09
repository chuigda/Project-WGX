package chr.wgx.render.data;

import chr.wgx.render.info.VertexInputInfo;

public abstract class RenderObject implements AutoCloseable {
    public final VertexInputInfo vertexInputInfo;

    public RenderObject(VertexInputInfo vertexInputInfo) {
        this.vertexInputInfo = vertexInputInfo;
    }
}
