package chr.wgx.render.gles2;

import chr.wgx.render.RenderException;
import chr.wgx.render.info.VertexInputInfo;
import tech.icey.gles2.GLES2;
import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.panama.buffer.PointerBuffer;

import java.lang.foreign.Arena;
import java.util.function.Function;

public final class GLES2Utils {
    public enum InformationKind {
        Shader, Program
    }

    private GLES2Utils() {}

    public static int getStatus(
            InformationKind kind,
            GLES2 gl,
            Arena arena,
            @enumtype(GLES2Constants.class) int statusKind,
            int handle
    ) {
        var value = IntBuffer.allocate(arena);
        switch (kind) {
            case Shader -> gl.glGetShaderiv(handle, statusKind, value);
            case Program -> gl.glGetProgramiv(handle, statusKind, value);
        }

        return value.read();
    }

    public static String getLog(InformationKind kind, GLES2 gl, Arena arena, int handle) {
        var logSize = getStatus(kind, gl, arena, GLES2Constants.GL_INFO_LOG_LENGTH, handle);
        var logBuffer = ByteBuffer.allocate(arena, logSize);
        switch (kind) {
            case Shader -> gl.glGetShaderInfoLog(handle, logSize, null, logBuffer);
            case Program -> gl.glGetProgramInfoLog(handle, logSize, null, logBuffer);
        }

        return logBuffer.readString();
    }

    public static int getShaderStatus(GLES2 gl, Arena arena, @enumtype(GLES2Constants.class) int statusKind, int shaderHandle) {
        return getStatus(InformationKind.Shader, gl, arena, statusKind, shaderHandle);
    }

    public static int getProgramStatus(GLES2 gl, Arena arena, @enumtype(GLES2Constants.class) int statusKind, int programHandle) {
        return getStatus(InformationKind.Program, gl, arena, statusKind, programHandle);
    }

    public static String getShaderLog(GLES2 gl, Arena arena, int shaderHandle) {
        return getLog(InformationKind.Shader, gl, arena, shaderHandle);
    }

    public static String getProgramLog(GLES2 gl, Arena arena, int programHandle) {
        return getLog(InformationKind.Program, gl, arena, programHandle);
    }

    public static void checkStatus(
            InformationKind kind,
            GLES2 gl,
            Arena arena,
            @enumtype(GLES2Constants.class) int statusKind,
            int handle,
            Function<String, RenderException> exceptionProvider
    ) throws RenderException {
        var status = getStatus(kind, gl, arena, statusKind, handle);
        if (status != GLES2Constants.GL_TRUE) {
            var errorMsg = getLog(kind, gl, arena, handle);
            throw exceptionProvider.apply(errorMsg);
        }
    }

    public static int loadShader(GLES2 gl, Arena arena, @enumtype(GLES2Constants.class) int shaderType, String shaderSource) throws RenderException {
        var shaderHandle = gl.glCreateShader(shaderType);
        gl.glShaderSource(shaderHandle, 1, new PointerBuffer(arena.allocateFrom(shaderSource)), null);
        gl.glCompileShader(shaderHandle);

        checkStatus(InformationKind.Shader, gl, arena, GLES2Constants.GL_COMPILE_STATUS, shaderHandle, msg ->
                new RenderException("无法编译 shader: " + msg));

        return shaderHandle;
    }

    public static void bindAttributes(GLES2 gl, Arena arena, int programHandle, VertexInputInfo info) {
        int index = 0;
        for (var attr : info.attributes) {
            var ty = attr.type;
            var name = attr.name;
            gl.glBindAttribLocation(programHandle, index, ByteBuffer.allocateString(arena, name));
            index = index + ty.glIndexSize;
        }
    }
}
