package chr.wgx.render.data;

import chr.wgx.render.info.VertexInputInfo;

public abstract class RenderObject {
    public final VertexInputInfo attributeInfo;

    public RenderObject(VertexInputInfo attributeInfo) {
        this.attributeInfo = attributeInfo;
    }
}
