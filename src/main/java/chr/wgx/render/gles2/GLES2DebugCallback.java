package chr.wgx.render.gles2;

import tech.icey.gles2.GLES2Constants;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.annotation.pointer;
import tech.icey.panama.annotation.unsigned;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public final class GLES2DebugCallback {
    private static void debugCallback(
            @enumtype(GLES2Constants.class) int source,
            @enumtype(GLES2Constants.class) int type,
            @unsigned int id,
            @enumtype(GLES2Constants.class) int severity,
            int length,
            @pointer(comment = "const GLchar*") MemorySegment message,
            @pointer(comment = "const void*") MemorySegment userParam
    ) {
        String messageString = new ByteBuffer(message).readString();
        Action1<String> action = getSeverityLoggingFunction(severity);
        action.accept(String.format("%s | %s | %s | %s | %s",
                describeDebugSource(source),
                describeDebugType(type),
                describeDebugSeverity(severity),
                Integer.toHexString(id),
                messageString
        ));

        if (severity == GLES2DebugFunctions.DEBUG_SEVERITY_HIGH) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("JVM 调用栈:\n");
            for (StackTraceElement stackTraceElement : stackTrace) {
                sb.append("\t").append(stackTraceElement).append("\n");
            }
            action.accept(sb.toString());
        }
    }

    private static Action1<String> getSeverityLoggingFunction(@enumtype(GLES2Constants.class) int severity) {
        Action1<String> action;
        if (severity == GLES2DebugFunctions.DEBUG_SEVERITY_HIGH) {
            action = logger::severe;
        } else if (severity == GLES2DebugFunctions.DEBUG_SEVERITY_MEDIUM) {
            action = logger::warning;
        } else if (severity == GLES2DebugFunctions.DEBUG_SEVERITY_LOW) {
            action = logger::info;
        } else {
            action = logger::fine;
        }
        return action;
    }

    private static String describeDebugSource(@enumtype(GLES2Constants.class) int source) {
        return switch (source) {
            case GLES2DebugFunctions.DEBUG_SOURCE_API -> "API";
            case GLES2DebugFunctions.DEBUG_SOURCE_WINDOW_SYSTEM -> "Window System";
            case GLES2DebugFunctions.DEBUG_SOURCE_SHADER_COMPILER -> "Shader Compiler";
            case GLES2DebugFunctions.DEBUG_SOURCE_THIRD_PARTY -> "Third Party";
            case GLES2DebugFunctions.DEBUG_SOURCE_APPLICATION -> "Application";
            case GLES2DebugFunctions.DEBUG_SOURCE_OTHER -> "Other";
            default -> "Unknown";
        };
    }

    private static String describeDebugType(@enumtype(GLES2Constants.class) int type) {
        return switch (type) {
            case GLES2DebugFunctions.DEBUG_TYPE_ERROR -> "Error";
            case GLES2DebugFunctions.DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "Deprecated Behavior";
            case GLES2DebugFunctions.DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "Undefined Behavior";
            case GLES2DebugFunctions.DEBUG_TYPE_PORTABILITY -> "Portability";
            case GLES2DebugFunctions.DEBUG_TYPE_PERFORMANCE -> "Performance";
            case GLES2DebugFunctions.DEBUG_TYPE_OTHER -> "Other";
            case GLES2DebugFunctions.DEBUG_TYPE_MARKER -> "Marker";
            default -> "Unknown";
        };
    }

    private static String describeDebugSeverity(@enumtype(GLES2Constants.class) int severity) {
        return switch (severity) {
            case GLES2DebugFunctions.DEBUG_SEVERITY_HIGH -> "High";
            case GLES2DebugFunctions.DEBUG_SEVERITY_MEDIUM -> "Medium";
            case GLES2DebugFunctions.DEBUG_SEVERITY_LOW -> "Low";
            case GLES2DebugFunctions.DEBUG_SEVERITY_NOTIFICATION -> "Notification";
            default -> "Unknown";
        };
    }

    private static final Logger logger = Logger.getLogger("gles2.debug");

    public static final MemorySegment DEBUG_CALLBACK;
    static {
        try {
            MethodHandle handle = MethodHandles
                    .lookup()
                    .findStatic(GLES2DebugCallback.class, "debugCallback", GLES2DebugFunctions.DESCRIPTOR$GLDEBUGPROC.toMethodType());
            DEBUG_CALLBACK = Linker.nativeLinker().upcallStub(handle, GLES2DebugFunctions.DESCRIPTOR$GLDEBUGPROC, Arena.global());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
