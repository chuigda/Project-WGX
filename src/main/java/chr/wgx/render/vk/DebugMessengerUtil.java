package chr.wgx.render.vk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.annotation.Pointer;
import club.doki7.ffm.ptr.BytePtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkDebugUtilsMessageSeverityFlagsEXT;
import club.doki7.vulkan.bitmask.VkDebugUtilsMessageTypeFlagsEXT;
import club.doki7.vulkan.datatype.VkDebugUtilsMessengerCallbackDataEXT;
import tech.icey.xjbutil.functional.Action1;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

public final class DebugMessengerUtil {
    private static final Logger logger = Logger.getLogger("vulkan.validation-layer");

    private static /* VkBool32 */ int debugCallback(
            @EnumType(VkDebugUtilsMessageSeverityFlagsEXT.class) int messageSeverity,
            @EnumType(VkDebugUtilsMessageTypeFlagsEXT.class) int ignoredMessageType,
            @Pointer(comment="const VkDebugUtilsMessengerCallbackDataEXT*") MemorySegment pCallbackData,
            @Pointer(comment="void*") MemorySegment ignoredPUserData
    ) {
        VkDebugUtilsMessengerCallbackDataEXT callbackData =
                new VkDebugUtilsMessengerCallbackDataEXT(pCallbackData.reinterpret(VkDebugUtilsMessengerCallbackDataEXT.BYTES));
        @Nullable BytePtr pMessage = callbackData.pMessage();
        String message = pMessage != null ? pMessage.readString() : "发生了未知错误, 没有诊断消息可用";

        Action1<String> action = getSeverityLoggingFunction(messageSeverity);
        action.accept(message);

        if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.ERROR) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("JVM 调用栈:\n");
            for (StackTraceElement stackTraceElement : stackTrace) {
                sb.append("\t").append(stackTraceElement).append("\n");
            }
            action.accept(sb.toString());
        }

        return VkConstants.FALSE;
    }

    private static @NotNull Action1<String> getSeverityLoggingFunction(@EnumType(VkDebugUtilsMessageSeverityFlagsEXT.class) int messageSeverity) {
        Action1<String> action;
        if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.ERROR) {
            action = logger::severe;
        } else if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.WARNING) {
            action = logger::warning;
        } else if (messageSeverity >= VkDebugUtilsMessageSeverityFlagsEXT.INFO) {
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
