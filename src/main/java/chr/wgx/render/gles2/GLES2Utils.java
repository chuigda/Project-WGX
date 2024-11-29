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

public final class GLES2Utils {
    private GLES2Utils() {}

    public static int getShaderStatus(GLES2 gl, Arena arena, @enumtype(GLES2Constants.class) int statusCode, int shaderHandle) {
        var value = IntBuffer.allocate(arena);
        gl.glGetShaderiv(shaderHandle, statusCode, value);
        return value.read();
    }

    public static String getShaderLog(GLES2 gl, Arena arena, int shaderHandle) {
        var logSize = getShaderStatus(gl, arena, GLES2Constants.GL_INFO_LOG_LENGTH, shaderHandle);
        var logBuffer = ByteBuffer.allocate(arena, logSize);
        gl.glGetShaderInfoLog(shaderHandle, logSize, null, logBuffer);
        return logBuffer.readString();
    }

    public static int loadShader(GLES2 gl, Arena arena, @enumtype(GLES2Constants.class) int shaderType, String shaderSource) throws RenderException {
        var shaderHandle = gl.glCreateShader(shaderType);
        gl.glShaderSource(shaderHandle, 1, new PointerBuffer(arena.allocateFrom(shaderSource)), null);
        gl.glCompileShader(shaderHandle);

        var status = getShaderStatus(gl, arena, GLES2Constants.GL_COMPILE_STATUS, shaderHandle);
        if (status != GLES2Constants.GL_TRUE) {
            throw new RenderException("无法编译 shader: " + getShaderLog(gl, arena, shaderHandle));
        }

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
