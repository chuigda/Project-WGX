package chr.wgx.render.gles2;

import chr.wgx.render.info.RenderPipelineCreateInfo;
import chr.wgx.render.info.RenderTaskInfo;
import chr.wgx.render.info.VertexInputInfo;
import tech.icey.gles2.GLES2;
import tech.icey.panama.buffer.IntBuffer;

import java.lang.foreign.Arena;

public final class Resource {
    private Resource() {}

    public interface Disposable {
        void dispose(GLES2 gles2);
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static final class Object implements Disposable {
        public final int vbo;
        public final VertexInputInfo attributeInfo;
        public final long vertexCount;

        public Object(int vbo, VertexInputInfo attributeInfo, long vertexCount) {
            this.vbo = vbo;
            this.attributeInfo = attributeInfo;
            this.vertexCount = vertexCount;
        }

        @Override
        public void dispose(GLES2 gles2) {
            try (Arena arena = Arena.ofConfined()) {
                IntBuffer pBuffer = IntBuffer.allocate(arena);
                pBuffer.write(vbo);

                gles2.glDeleteBuffers(1, pBuffer);
            }
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static final class Pipeline implements Disposable {
        public final RenderPipelineCreateInfo createInfo;
        public final int programHandle;

        public Pipeline(RenderPipelineCreateInfo createInfo, int programHandle) {
            this.createInfo = createInfo;
            this.programHandle = programHandle;
        }

        @Override
        public void dispose(GLES2 gles2) {
            try (Arena arena = Arena.ofConfined()) {
                IntBuffer pBuffer = IntBuffer.allocate(arena);
                pBuffer.write(programHandle);

                gles2.glDeleteProgram(programHandle);
            }
        }
    }

    public static final class Task implements Disposable {
        public final RenderTaskInfo taskInfo;

        public Task(RenderTaskInfo taskInfo) {
            this.taskInfo = taskInfo;
        }

        @Override
        public void dispose(GLES2 gles2) {
            // TODO
        }
    }
}
