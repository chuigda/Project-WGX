package chr.wgx.render.gles2;

import chr.wgx.render.gles2.glext.KHR_debug;
import club.doki7.gles2.GLES2Constants;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.annotation.Pointer;
import club.doki7.ffm.annotation.Unsigned;
import club.doki7.ffm.ptr.BytePtr;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public final class GLES2DebugCallback {
    private static void debugCallback(
            @EnumType(GLES2Constants.class) int source,
            @EnumType(GLES2Constants.class) int type,
            @Unsigned int id,
            @EnumType(GLES2Constants.class) int severity,
            int ignoredLength,
            @Pointer(comment = "const GLchar*") MemorySegment message,
            @Pointer(comment = "const void*") MemorySegment ignoredUserParam
    ) {
        String messageString = new BytePtr(message).readString();
        Action1<String> action = getSeverityLoggingFunction(severity);
        action.accept(String.format("%s | %s | %s | %s | %s",
                describeDebugSource(source),
                describeDebugType(type),
                describeDebugSeverity(severity),
                Integer.toHexString(id),
                messageString
        ));

        if (severity == KHR_debug.DEBUG_SEVERITY_HIGH) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("JVM 调用栈:\n");
            for (StackTraceElement stackTraceElement : stackTrace) {
                sb.append("\t").append(stackTraceElement).append("\n");
            }
            action.accept(sb.toString());
        }
    }

    private static Action1<String> getSeverityLoggingFunction(@EnumType(GLES2Constants.class) int severity) {
        Action1<String> action;
        if (severity == KHR_debug.DEBUG_SEVERITY_HIGH) {
            action = logger::severe;
        } else if (severity == KHR_debug.DEBUG_SEVERITY_MEDIUM) {
            action = logger::warning;
        } else if (severity == KHR_debug.DEBUG_SEVERITY_LOW) {
            action = logger::info;
        } else {
            action = logger::fine;
        }
        return action;
    }

    private static String describeDebugSource(@EnumType(GLES2Constants.class) int source) {
        return switch (source) {
            case KHR_debug.DEBUG_SOURCE_API -> "API";
            case KHR_debug.DEBUG_SOURCE_WINDOW_SYSTEM -> "Window System";
            case KHR_debug.DEBUG_SOURCE_SHADER_COMPILER -> "Shader Compiler";
            case KHR_debug.DEBUG_SOURCE_THIRD_PARTY -> "Third Party";
            case KHR_debug.DEBUG_SOURCE_APPLICATION -> "Application";
            case KHR_debug.DEBUG_SOURCE_OTHER -> "Other";
            default -> "Unknown";
        };
    }

    private static String describeDebugType(@EnumType(GLES2Constants.class) int type) {
        return switch (type) {
            case KHR_debug.DEBUG_TYPE_ERROR -> "Error";
            case KHR_debug.DEBUG_TYPE_DEPRECATED_BEHAVIOR -> "Deprecated Behavior";
            case KHR_debug.DEBUG_TYPE_UNDEFINED_BEHAVIOR -> "Undefined Behavior";
            case KHR_debug.DEBUG_TYPE_PORTABILITY -> "Portability";
            case KHR_debug.DEBUG_TYPE_PERFORMANCE -> "Performance";
            case KHR_debug.DEBUG_TYPE_OTHER -> "Other";
            case KHR_debug.DEBUG_TYPE_MARKER -> "Marker";
            default -> "Unknown";
        };
    }

    private static String describeDebugSeverity(@EnumType(GLES2Constants.class) int severity) {
        return switch (severity) {
            case KHR_debug.DEBUG_SEVERITY_HIGH -> "High";
            case KHR_debug.DEBUG_SEVERITY_MEDIUM -> "Medium";
            case KHR_debug.DEBUG_SEVERITY_LOW -> "Low";
            case KHR_debug.DEBUG_SEVERITY_NOTIFICATION -> "Notification";
            default -> "Unknown";
        };
    }

    private static final Logger logger = Logger.getLogger("gles2.debug");

    public static final MemorySegment DEBUG_CALLBACK;
    static {
        try {
            MethodHandle handle = MethodHandles
                    .lookup()
                    .findStatic(GLES2DebugCallback.class, "debugCallback", KHR_debug.DESCRIPTOR$GLDEBUGPROC.toMethodType());
            DEBUG_CALLBACK = Linker.nativeLinker().upcallStub(handle, KHR_debug.DESCRIPTOR$GLDEBUGPROC, Arena.global());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
