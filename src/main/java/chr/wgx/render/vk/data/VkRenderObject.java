package chr.wgx.render.vk.data;

import chr.wgx.render.data.RenderObject;
import chr.wgx.render.info.VertexInputInfo;
import chr.wgx.render.vk.IVkDisposable;
import chr.wgx.render.vk.Resource;
import chr.wgx.render.vk.VulkanRenderEngineContext;

public final class VkRenderObject extends RenderObject implements IVkDisposable {
    public final Resource.Buffer vertexBuffer;
    public final Resource.Buffer indexBuffer;
    public final int vertexCount;
    public final int indexCount;

    public VkRenderObject(
            VertexInputInfo attributeInfo,
            Resource.Buffer vertexBuffer,
            Resource.Buffer indexBuffer,
            int vertexCount,
            int indexCount
    ) {
        super(attributeInfo);

        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
    }

    @Override
    public void dispose(VulkanRenderEngineContext cx) {
        vertexBuffer.dispose(cx);
        indexBuffer.dispose(cx);
    }
}
