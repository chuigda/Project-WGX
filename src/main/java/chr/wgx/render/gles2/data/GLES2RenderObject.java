package chr.wgx.render.gles2.data;

import chr.wgx.render.data.RenderObject;
import chr.wgx.render.gles2.IGLES2Disposable;
import chr.wgx.render.info.VertexInputInfo;
import club.doki7.gles2.GLES2;
import club.doki7.ffm.ptr.IntPtr;

import java.lang.foreign.Arena;

public final class GLES2RenderObject extends RenderObject implements IGLES2Disposable {
    public final int vertexVBO;
    public final int indexVBO;
    public final int vertexCount;
    public final int indexCount;

    public GLES2RenderObject(
            VertexInputInfo attributeInfo,
            int vertexVBO,
            int indexVBO,
            int vertexCount,
            int indexCount
    ) {
        super(attributeInfo);
        this.vertexVBO = vertexVBO;
        this.indexVBO = indexVBO;
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
    }

    @Override
    public void dispose(GLES2 gles2) {
        try (Arena arena = Arena.ofConfined()) {
            IntPtr pBuffer = IntPtr.allocate(arena);
            pBuffer.write(vertexVBO);

            gles2.deleteBuffers(1, pBuffer);
        }
    }
}
