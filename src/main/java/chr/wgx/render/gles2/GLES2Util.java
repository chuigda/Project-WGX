package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.panama.buffer.PointerBuffer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class GLES2Util {
    public static int compileShaderProgram(
            GLES2 gles2,
            String vertexShaderSource,
            String fragmentShaderSource
    ) throws RenderException {
        int vertexShader = gles2.glCreateShader(GLES2Constants.GL_VERTEX_SHADER);
        int fragmentShader = gles2.glCreateShader(GLES2Constants.GL_FRAGMENT_SHADER);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment vertexShaderSourceSegment = arena.allocateFrom(vertexShaderSource);
            MemorySegment fragmentShaderSourceSegment = arena.allocateFrom(fragmentShaderSource);
            PointerBuffer pVertexShaderSource = PointerBuffer.allocate(arena);
            PointerBuffer pFragmentShaderSource = PointerBuffer.allocate(arena);
            pVertexShaderSource.write(vertexShaderSourceSegment);
            pFragmentShaderSource.write(fragmentShaderSourceSegment);

            gles2.glShaderSource(vertexShader, 1, pVertexShaderSource, null);
            gles2.glShaderSource(fragmentShader, 1, pFragmentShaderSource, null);

            IntBuffer pStatus = IntBuffer.allocate(arena);

            gles2.glCompileShader(vertexShader);
            gles2.glGetShaderiv(vertexShader, GLES2Constants.GL_COMPILE_STATUS, pStatus);
            if (pStatus.read() == GLES2Constants.GL_FALSE) {
                gles2.glGetShaderiv(vertexShader, GLES2Constants.GL_INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                ByteBuffer infoLog = ByteBuffer.allocate(arena, infoLogLength);
                gles2.glGetShaderInfoLog(vertexShader, infoLogLength, pStatus, infoLog);
                throw new RenderException("顶点着色器编译失败: " + infoLog.readString());
            }

            gles2.glCompileShader(fragmentShader);
            gles2.glGetShaderiv(fragmentShader, GLES2Constants.GL_COMPILE_STATUS, pStatus);
            if (pStatus.read() == GLES2Constants.GL_FALSE) {
                gles2.glGetShaderiv(fragmentShader, GLES2Constants.GL_INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                ByteBuffer infoLog = ByteBuffer.allocate(arena, infoLogLength);
                gles2.glGetShaderInfoLog(fragmentShader, infoLogLength, pStatus, infoLog);
                throw new RenderException("片段着色器编译失败: " + infoLog.readString());
            }

            int program = gles2.glCreateProgram();
            gles2.glAttachShader(program, vertexShader);
            gles2.glAttachShader(program, fragmentShader);
            gles2.glLinkProgram(program);
            gles2.glGetProgramiv(program, GLES2Constants.GL_LINK_STATUS, pStatus);
            if (pStatus.read() == GLES2Constants.GL_FALSE) {
                gles2.glGetProgramiv(program, GLES2Constants.GL_INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                ByteBuffer infoLog = ByteBuffer.allocate(arena, infoLogLength);
                gles2.glGetProgramInfoLog(program, infoLogLength, pStatus, infoLog);

                gles2.glDeleteProgram(program);
                throw new RenderException("着色器程序链接失败: " + infoLog.readString());
            }

            return program;
        }
    }
}
