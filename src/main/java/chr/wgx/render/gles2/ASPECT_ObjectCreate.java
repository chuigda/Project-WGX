package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.data.RenderObject;
import chr.wgx.render.gles2.data.GLES2RenderObject;
import chr.wgx.render.info.ObjectCreateInfo;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.buffer.IntBuffer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public final class ASPECT_ObjectCreate {
    ASPECT_ObjectCreate(GLES2RenderEngine engine) {
        this.engine = engine;
    }

    public List<RenderObject> createObjectImpl(List<ObjectCreateInfo> infoList) throws RenderException {
        GLES2 gles2 = engine.gles2;

        List<RenderObject> ret = new ArrayList<>();
        try (Arena arena = Arena.ofConfined()) {
            IntBuffer pVBO = IntBuffer.allocate(arena, (long) infoList.size() * 2);
            gles2.glGenBuffers(infoList.size() * 2, pVBO);

            for (int i = 0; i < infoList.size(); i++) {
                ObjectCreateInfo info = infoList.get(i);

                int vertexVBO = pVBO.read((long) i * 2);
                int indexVBO = pVBO.read((long) i * 2 + 1);

                MemorySegment pVertices;
                MemorySegment pIndices;
                if (info.pVertices.isNative()) {
                    pVertices = info.pVertices;
                } else {
                    pVertices = arena.allocate(info.pVertices.byteSize());
                    pVertices.copyFrom(info.pVertices);
                }

                if (info.pIndices.isNative()) {
                    pIndices = info.pIndices;
                } else {
                    pIndices = arena.allocate(info.pIndices.byteSize());
                    pIndices.copyFrom(info.pIndices);
                }

                gles2.glBindBuffer(GLES2Constants.GL_ARRAY_BUFFER, vertexVBO);
                gles2.glBufferData(
                        GLES2Constants.GL_ARRAY_BUFFER,
                        pVertices.byteSize(),
                        pVertices,
                        GLES2Constants.GL_STATIC_DRAW
                );

                gles2.glBindBuffer(GLES2Constants.GL_ELEMENT_ARRAY_BUFFER, indexVBO);
                gles2.glBufferData(
                        GLES2Constants.GL_ELEMENT_ARRAY_BUFFER,
                        pIndices.byteSize(),
                        pIndices,
                        GLES2Constants.GL_STATIC_DRAW
                );

                int status = gles2.glGetError();
                if (status != GLES2Constants.GL_NO_ERROR) {
                    throw new RenderException("创建顶点缓冲对象并上传数据失败: " + status);
                }

                GLES2RenderObject object = new GLES2RenderObject(
                        info.vertexInputInfo,
                        vertexVBO,
                        indexVBO,
                        (int) (info.pVertices.byteSize() / info.vertexInputInfo.stride),
                        (int) (info.pIndices.byteSize() / Integer.BYTES)
                );

                ret.add(object);
                engine.objects.add(object);
            }

            return ret;
        }
    }

    private final GLES2RenderEngine engine;
}
