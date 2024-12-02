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
import java.util.function.ObjIntConsumer;

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

    public static int loadShader(
            GLES2 gl,
            Arena arena,
            @enumtype(GLES2Constants.class) int shaderType,
            String shaderSource
    ) throws RenderException {
        var shaderHandle = gl.glCreateShader(shaderType);
        var ppString = PointerBuffer.allocate(arena);
        ppString.write(ByteBuffer.allocateString(arena, shaderSource));
        gl.glShaderSource(shaderHandle, 1, ppString, null);
        gl.glCompileShader(shaderHandle);

        checkStatus(InformationKind.Shader, gl, arena, GLES2Constants.GL_COMPILE_STATUS, shaderHandle, msg ->
                new RenderException("无法编译 shader: " + msg));

        return shaderHandle;
    }

    /// 创建并初始化缓存
    /// @implSpec 这个函数会被绑定新的缓存到 {@param target}
    public static int initBuffer(
            GLES2 gl,
            Arena arena,
            @enumtype(GLES2Constants.class) int target,
            MemorySegment bufferData
    ) throws RenderException {
        var bufferPtr = IntBuffer.allocate(arena);
        var objectSize = bufferData.byteSize();
        // no more performance
        gl.glGenBuffers(1, bufferPtr);

        // the program should not assume which buffer WAS bound to ARRAY_BUFFER,
        // so it is safe to bind the buffer to ARRAY_BUFFER here
        int bufferHandle = bufferPtr.read();
        gl.glBindBuffer(target, bufferHandle);

        gl.glBufferData(target,
                objectSize,
                GLES2Utils.makeSureNative(arena, bufferData),
                GLES2Constants.GL_STATIC_DRAW
        );

        int error = gl.glGetError();
        if (error != GLES2Constants.GL_NO_ERROR) {
            gl.glDeleteBuffers(1, bufferPtr);
            throw new RenderException("无法初始化缓冲: " + error);
        }

        return bufferHandle;
    }

    /// 绑定并启用 {@param info} 所提供的 {@link chr.wgx.render.info.VertexInputInfo.Attribute}
    public static void initializeAttributes(GLES2 gl, Arena arena, int programHandle, VertexInputInfo info) {
        var stride = info.stride;

        forEachAttribute(info, (attr, index) -> {
            gl.glEnableVertexAttribArray(index);
            gl.glVertexAttribPointer(
                    index,
                    attr.type.componentCount,
                    GLES2Constants.GL_FLOAT,
                    (byte) GLES2Constants.GL_FALSE,
                    stride,
                    MemorySegment.ofAddress(attr.byteOffset)
            );
        });
    }

    public static void forEachAttribute(VertexInputInfo info, ObjIntConsumer<VertexInputInfo.Attribute> block) {
        int index = 0;
        for (var attr : info.attributes) {
            block.accept(attr, index);
            index = index + attr.type.glIndexSize;
        }
    }

    // TODO: move this to elsewhere
    public static MemorySegment makeSureNative(Arena arena, MemorySegment memory) {
        if (memory.isNative()) return memory;
        return arena.allocate(memory.byteSize())
                .copyFrom(memory);
    }
}
