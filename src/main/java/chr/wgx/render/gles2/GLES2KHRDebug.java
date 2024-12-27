package chr.wgx.render.gles2;

import org.jetbrains.annotations.Nullable;
import tech.icey.panama.IPointer;
import tech.icey.panama.RawFunctionLoader;
import tech.icey.panama.annotation.unsigned;
import tech.icey.panama.buffer.IntBuffer;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Objects;

public final class GLES2KHRDebug {
    public static final int DEBUG_SOURCE_API                                 = 0x8246;
    public static final int DEBUG_SOURCE_WINDOW_SYSTEM                       = 0x8247;
    public static final int DEBUG_SOURCE_SHADER_COMPILER                     = 0x8248;
    public static final int DEBUG_SOURCE_THIRD_PARTY                         = 0x8249;
    public static final int DEBUG_SOURCE_APPLICATION                         = 0x824A;
    public static final int DEBUG_SOURCE_OTHER                               = 0x824B;

    public static final int DEBUG_TYPE_ERROR                                 = 0x824C;
    public static final int DEBUG_TYPE_DEPRECATED_BEHAVIOR                   = 0x824D;
    public static final int DEBUG_TYPE_UNDEFINED_BEHAVIOR                    = 0x824E;
    public static final int DEBUG_TYPE_PORTABILITY                           = 0x824F;
    public static final int DEBUG_TYPE_PERFORMANCE                           = 0x8250;
    public static final int DEBUG_TYPE_OTHER                                 = 0x8251;
    public static final int DEBUG_TYPE_MARKER                                = 0x8268;

    public static final int DEBUG_SEVERITY_HIGH                              = 0x9146;
    public static final int DEBUG_SEVERITY_MEDIUM                            = 0x9147;
    public static final int DEBUG_SEVERITY_LOW                               = 0x9148;
    public static final int DEBUG_SEVERITY_NOTIFICATION                      = 0x826B;

    public void glDebugMessageCallback(MemorySegment callback, @Nullable IPointer userParam) {
        try {
            HANDLE$glDebugMessageCallback.invokeExact(
                    callback,
                    (MemorySegment) (userParam == null ? MemorySegment.NULL : userParam.segment())
            );
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public void glDebugMessageControl(
            int source,
            int type,
            int severity,
            int count,
            @Nullable @unsigned IntBuffer ids,
            boolean enabled) {
        try {
            HANDLE$glDebugMessageControl.invokeExact(
                    source,
                    type,
                    severity,
                    count,
                    (MemorySegment) (ids == null ? MemorySegment.NULL : ids.segment()),
                    enabled
            );
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public GLES2KHRDebug(RawFunctionLoader loader) {
        MemorySegment segment$glDebugMessageCallback = loader.apply("glDebugMessageCallback");
        MemorySegment segment$glDebugMessageControl = loader.apply("glDebugMessageControl");

        this.HANDLE$glDebugMessageCallback = Objects.requireNonNull(
                RawFunctionLoader.link(segment$glDebugMessageCallback, DESCRIPTOR$glDebugMessageCallback)
        );
        this.HANDLE$glDebugMessageControl = Objects.requireNonNull(
                RawFunctionLoader.link(segment$glDebugMessageControl, DESCRIPTOR$glDebugMessageControl)
        );
    }

    public final MethodHandle HANDLE$glDebugMessageCallback;
    public final MethodHandle HANDLE$glDebugMessageControl;

    public static final FunctionDescriptor DESCRIPTOR$glDebugMessageCallback = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // DEBUGPROC callback,
            ValueLayout.ADDRESS  // const void *userParam
    );

    public static final FunctionDescriptor DESCRIPTOR$glDebugMessageControl = FunctionDescriptor.ofVoid(
            ValueLayout.JAVA_INT,    // GLenum source,
            ValueLayout.JAVA_INT,    // GLenum type,
            ValueLayout.JAVA_INT,    // GLenum severity,
            ValueLayout.JAVA_INT,    // GLsizei count,
            ValueLayout.ADDRESS,     // const GLuint *ids,
            ValueLayout.JAVA_BOOLEAN // GLboolean enabled
    );

    public static final FunctionDescriptor DESCRIPTOR$GLDEBUGPROC = FunctionDescriptor.ofVoid(
            ValueLayout.JAVA_INT,    // GLenum source,
            ValueLayout.JAVA_INT,    // GLenum type,
            ValueLayout.JAVA_INT,    // GLuint id,
            ValueLayout.JAVA_INT,    // GLenum severity,
            ValueLayout.JAVA_INT,    // GLsizei length,
            ValueLayout.ADDRESS,     // const GLchar *message,
            ValueLayout.ADDRESS      // const void *userParam
    );
}
