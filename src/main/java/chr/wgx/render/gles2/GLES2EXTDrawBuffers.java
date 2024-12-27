package chr.wgx.render.gles2;

import tech.icey.panama.RawFunctionLoader;
import tech.icey.panama.buffer.IntBuffer;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

public class GLES2EXTDrawBuffers {
    public static final int GL_MAX_COLOR_ATTACHMENTS_EXT = 0x8CDF;

    public static final int GL_COLOR_ATTACHMENT0_EXT = 0x8CE0;
    public static final int GL_COLOR_ATTACHMENT1_EXT = 0x8CE1;
    public static final int GL_COLOR_ATTACHMENT2_EXT = 0x8CE2;
    public static final int GL_COLOR_ATTACHMENT3_EXT = 0x8CE3;
    public static final int GL_COLOR_ATTACHMENT4_EXT = 0x8CE4;
    public static final int GL_COLOR_ATTACHMENT5_EXT = 0x8CE5;
    public static final int GL_COLOR_ATTACHMENT6_EXT = 0x8CE6;
    public static final int GL_COLOR_ATTACHMENT7_EXT = 0x8CE7;
    public static final int GL_COLOR_ATTACHMENT8_EXT = 0x8CE8;
    public static final int GL_COLOR_ATTACHMENT9_EXT = 0x8CE9;
    public static final int GL_COLOR_ATTACHMENT10_EXT = 0x8CEA;
    public static final int GL_COLOR_ATTACHMENT11_EXT = 0x8CEB;
    public static final int GL_COLOR_ATTACHMENT12_EXT = 0x8CEC;
    public static final int GL_COLOR_ATTACHMENT13_EXT = 0x8CED;
    public static final int GL_COLOR_ATTACHMENT14_EXT = 0x8CEE;
    public static final int GL_COLOR_ATTACHMENT15_EXT = 0x8CEF;
    public static final int GL_DEPTH_ATTACHMENT_EXT = 0x8D00;
    public static final int GL_STENCIL_ATTACHMENT_EXT = 0x8D20;

    public void glDrawBuffersEXT(long n, IntBuffer bufs) {
        try (Arena arena = Arena.ofConfined()) {
            HANDLE$glDrawBuffersEXT.invokeExact(MemorySegment.ofAddress(n), bufs.segment());
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public GLES2EXTDrawBuffers(RawFunctionLoader loader) {
        MemorySegment pfn = loader.apply("glDrawBuffersEXT");
        HANDLE$glDrawBuffersEXT = Objects.requireNonNull(RawFunctionLoader.link(pfn, DESCRIPTOR$glDrawBuffersEXT));
    }

    private final MethodHandle HANDLE$glDrawBuffersEXT;

    public static final FunctionDescriptor DESCRIPTOR$glDrawBuffersEXT = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // GLsizei n
            ValueLayout.ADDRESS  // const GLenum *bufs
    );
}
