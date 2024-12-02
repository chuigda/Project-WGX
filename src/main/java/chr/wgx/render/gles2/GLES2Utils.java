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
import java.lang.foreign.MemorySegment;
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

    public static int getBufferStatus(
            GLES2 gl,
            Arena arena,
            @enumtype(GLES2Constants.class) int bufferKind,
            @enumtype(GLES2Constants.class) int statusKind
    ) {
        IntBuffer result = IntBuffer.allocate(arena);
        gl.glGetBufferParameteriv(bufferKind, statusKind, result);
        return result.read();
    }

    /// 检查特定状态，并在错误时调用 {@param onException}
    /// @param onException 错误时调用一次，可以包含副作用。接受一段错误信息，返回一个会被立刻抛出的 {@link RenderException}。
    public static void checkStatus(
            InformationKind kind,
            GLES2 gl,
            Arena arena,
            @enumtype(GLES2Constants.class) int statusKind,
            int handle,
            Function<String, RenderException> onException
    ) throws RenderException {
        var status = getStatus(kind, gl, arena, statusKind, handle);
        if (status != GLES2Constants.GL_TRUE) {
            var errorMsg = getLog(kind, gl, arena, handle);
            throw onException.apply(errorMsg);
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

    /// 创建并初始化缓存
    /// @implSpec 这个函数会修改当前被绑定到 {@param target} 的缓存
    public static int initBuffer(GLES2 gl, Arena arena, @enumtype(GLES2Constants.class) int target, MemorySegment bufferData) throws RenderException {
        var bufferPtr = IntBuffer.allocate(arena);
        var bufferHandle = bufferPtr.read();
        var objectSize = bufferData.byteSize();
        // no more performance
        gl.glGenBuffers(1, bufferPtr);
        // the program should not assume which buffer WAS bound to ARRAY_BUFFER,
        // so it is safe to bind the buffer to ARRAY_BUFFER here
        gl.glBindBuffer(target, bufferHandle);
        gl.glBufferData(target,
                objectSize,
                GLES2Utils.makeSureNative(arena, bufferData),
                GLES2Constants.GL_STATIC_DRAW
        );

        // check full write
        var bufferSize = GLES2Utils.getBufferStatus(gl, arena, target, GLES2Constants.GL_BUFFER_SIZE);
        if (bufferSize != objectSize) {
            gl.glDeleteBuffers(1, bufferPtr);
            // out of memory
            throw new RenderException("无法创建对象: 内存不足 (" + bufferSize + "/" + objectSize + ")");
        }

        return bufferHandle;
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

    // TODO: move this to elsewhere
    public static MemorySegment makeSureNative(Arena arena, MemorySegment memory) {
        if (memory.isNative()) return memory;
        return arena.allocate(memory.byteSize())
                .copyFrom(memory);
    }
}
