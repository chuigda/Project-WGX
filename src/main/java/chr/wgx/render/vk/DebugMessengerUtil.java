package chr.wgx.render.vk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.annotation.pointer;
import tech.icey.panama.buffer.ByteBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.bitmask.VkDebugUtilsMessageSeverityFlagsEXT;
import tech.icey.vk4j.bitmask.VkDebugUtilsMessageTypeFlagsEXT;
import tech.icey.vk4j.datatype.VkDebugUtilsMessengerCallbackDataEXT;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public final class DebugMessengerUtil {
    private static final Logger logger = Logger.getLogger("vulkan.validation-layer");

    private static /* VkBool32 */ int debugCallback(
            @enumtype(VkDebugUtilsMessageSeverityFlagsEXT.class) int messageSeverity,
            @enumtype(VkDebugUtilsMessageTypeFlagsEXT.class) int ignoredMessageType,
            @pointer(comment="const VkDebugUtilsMessengerCallbackDataEXT*") MemorySegment pCallbackData,
            @pointer(comment="void*") MemorySegment ignoredPUserData
    ) {
        VkDebugUtilsMessengerCallbackDataEXT callbackData =
                new VkDebugUtilsMessengerCallbackDataEXT(pCallbackData.reinterpret(VkDebugUtilsMessengerCallbackDataEXT.SIZE));
        @Nullable ByteBuffer pMessage = callbackData.pMessage();
        String message = pMessage != null ? pMessage.readString() : "发生了未知错误, 没有诊断消息可用";

        Action1<String> action = getSeverityLoggingFunction(messageSeverity);
        action.accept(message);

        if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("JVM 调用栈:\n");
            for (StackTraceElement stackTraceElement : stackTrace) {
                sb.append("\t").append(stackTraceElement).append("\n");
            }
            action.accept(sb.toString());
        }

        return Constants.VK_FALSE;
    }

    private static @NotNull Action1<String> getSeverityLoggingFunction(@enumtype(VkDebugUtilsMessageSeverityFlagsEXT.class) int messageSeverity) {
        Action1<String> action;
        if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) {
            action = logger::severe;
        } else if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) {
            action = logger::warning;
        } else if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT) {
            action = logger::info;
        } else {
            action = logger::fine;
        }
        return action;
    }

    public static final MemorySegment DEBUG_CALLBACK_PTR;
    static {
        try {
            FunctionDescriptor descriptor = FunctionDescriptor.of(
                    ValueLayout.JAVA_INT, // (return value) VkBool32
                    ValueLayout.JAVA_INT, // int messageSeverity
                    ValueLayout.JAVA_INT, // int messageType
                    ValueLayout.ADDRESS,  // const VkDebugUtilsMessengerCallbackDataEXT* pCallbackData
                    ValueLayout.ADDRESS   // void* pUserData
            );
            MethodHandle handle = MethodHandles
                    .lookup()
                    .findStatic(DebugMessengerUtil.class, "debugCallback", descriptor.toMethodType());

            DEBUG_CALLBACK_PTR = Linker.nativeLinker().upcallStub(handle, descriptor, Arena.global());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
