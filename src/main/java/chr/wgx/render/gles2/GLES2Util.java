package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import club.doki7.gles2.GLES2;
import club.doki7.ffm.ptr.BytePtr;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.ffm.ptr.PointerPtr;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public final class GLES2Util {
    public static int compileShaderProgram(
            GLES2 gles2,
            String vertexShaderSource,
            String fragmentShaderSource
    ) throws RenderException {
        int vertexShader = gles2.createShader(GLES2.VERTEX_SHADER);
        int fragmentShader = gles2.createShader(GLES2.FRAGMENT_SHADER);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment vertexShaderSourceSegment = arena.allocateFrom(vertexShaderSource);
            MemorySegment fragmentShaderSourceSegment = arena.allocateFrom(fragmentShaderSource);
            PointerPtr pVertexShaderSource = PointerPtr.allocate(arena);
            PointerPtr pFragmentShaderSource = PointerPtr.allocate(arena);
            pVertexShaderSource.write(vertexShaderSourceSegment);
            pFragmentShaderSource.write(fragmentShaderSourceSegment);

            gles2.shaderSource(vertexShader, 1, pVertexShaderSource, null);
            gles2.shaderSource(fragmentShader, 1, pFragmentShaderSource, null);

            IntPtr pStatus = IntPtr.allocate(arena);

            gles2.compileShader(vertexShader);
            gles2.getShaderiv(vertexShader, GLES2.COMPILE_STATUS, pStatus);
            if (pStatus.read() == GLES2.FALSE) {
                gles2.getShaderiv(vertexShader, GLES2.INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                BytePtr infoLog = BytePtr.allocate(arena, infoLogLength);
                gles2.getShaderInfoLog(vertexShader, infoLogLength, pStatus, infoLog);
                throw new RenderException("顶点着色器编译失败: " + infoLog.readString());
            }

            gles2.compileShader(fragmentShader);
            gles2.getShaderiv(fragmentShader, GLES2.COMPILE_STATUS, pStatus);
            if (pStatus.read() == GLES2.FALSE) {
                gles2.getShaderiv(fragmentShader, GLES2.INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                BytePtr infoLog = BytePtr.allocate(arena, infoLogLength);
                gles2.getShaderInfoLog(fragmentShader, infoLogLength, pStatus, infoLog);
                throw new RenderException("片段着色器编译失败: " + infoLog.readString());
            }

            int program = gles2.createProgram();
            gles2.attachShader(program, vertexShader);
            gles2.attachShader(program, fragmentShader);
            gles2.linkProgram(program);
            gles2.getProgramiv(program, GLES2.LINK_STATUS, pStatus);
            if (pStatus.read() == GLES2.FALSE) {
                gles2.getProgramiv(program, GLES2.INFO_LOG_LENGTH, pStatus);
                int infoLogLength = pStatus.read();

                BytePtr infoLog = BytePtr.allocate(arena, infoLogLength);
                gles2.getProgramInfoLog(program, infoLogLength, pStatus, infoLog);

                gles2.deleteProgram(program);
                throw new RenderException("着色器程序链接失败: " + infoLog.readString());
            }

            return program;
        }
    }
}
