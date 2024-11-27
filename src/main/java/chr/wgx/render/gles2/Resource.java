package chr.wgx.render.gles2;

import chr.wgx.render.info.VertexInputInfo;
import tech.icey.gles2.GLES2;
import tech.icey.panama.buffer.IntBuffer;

import java.lang.foreign.Arena;

public final class Resource {
    @SuppressWarnings("ClassCanBeRecord")
    public static final class Object {
        public final int glHandle;
        public final VertexInputInfo attributeInfo;
        public final long vertexCount;

        public Object(int glHandle, VertexInputInfo attributeInfo, long vertexCount) {
            this.glHandle = glHandle;
            this.attributeInfo = attributeInfo;
            this.vertexCount = vertexCount;
        }

        public void dispose(GLES2 gles2) {
            try (Arena arena = Arena.ofConfined()) {
                IntBuffer pBuffer = IntBuffer.allocate(arena);
                pBuffer.write(glHandle);

                gles2.glDeleteBuffers(1, pBuffer);
            }
        }
    }
}
