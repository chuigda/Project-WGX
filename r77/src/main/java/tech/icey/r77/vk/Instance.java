package tech.icey.r77.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import tech.icey.util.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK13.*;

public class Instance implements AutoCloseable {
    public Instance(String appName, boolean validation) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer appNameBuf = stack.UTF8(appName);
            ByteBuffer engineNameBuf = stack.UTF8("R77 Advanced Rendering Infrastructure");
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
                    .pApplicationName(appNameBuf)
                    .applicationVersion(1)
                    .pEngineName(engineNameBuf)
                    .engineVersion(0)
                    .apiVersion(VK_API_VERSION_1_3);

            Set<String> validationLayers = getSupportedValidationLayers();
            if (validation && validationLayers.isEmpty()) {
                logger.log(Logger.Level.WARN, "Vulkan 校验被要求启用，但当前平台不支持任何 Vulkan 校验层");
                validation = false;
            }

            List<String> validationLayersUsed = new ArrayList<>();
            if (validationLayers.contains("VK_LAYER_KHRONOS_validation")) {
                logger.log(Logger.Level.INFO, "使用标准的 VK_LAYER_KHRONOS_validation 校验层");
                validationLayersUsed.add("VK_LAYER_KHRONOS_validation");
            } else if (validationLayers.contains("VK_LAYER_LUNARG_standard_validation")) {
                logger.log(Logger.Level.INFO, "使用 VK_LAYER_LUNARG_standard_validation 校验层（旧）");
                validationLayersUsed.add("VK_LAYER_LUNARG_standard_validation");
            } else {
                logger.log(Logger.Level.WARN, "Vulkan 校验被要求启用，但当前平台不支持任何 R77 已知的 Vulkan 校验层");
                validation = false;
            }

            PointerBuffer requiredLayersBuf = null;
            if (validation) {
                requiredLayersBuf = stack.mallocPointer(validationLayersUsed.size());
                for (int i = 0; i < validationLayersUsed.size(); i++) {
                    requiredLayersBuf.put(i, stack.ASCII(validationLayersUsed.get(i)));
                }
            }

            PointerBuffer glfwExtensionsBuf = GLFWVulkan.glfwGetRequiredInstanceExtensions();
            if (glfwExtensionsBuf == null) {
                throw new RuntimeException("无法获取 GLFW 所需的 Vulkan 实例扩展");
            }

            Set<String> instanceExtensions = getInstanceExtensions();
            while (glfwExtensionsBuf.hasRemaining()) {
                String extension = glfwExtensionsBuf.getStringASCII();
                if (!instanceExtensions.contains(extension)) {
                    throw new RuntimeException(String.format("GLFW 所需的 Vulkan 实例扩展 %s 未被支持", extension));
                }
            }
            glfwExtensionsBuf.rewind();

            if (validation && !instanceExtensions.contains(VK_EXT_DEBUG_UTILS_EXTENSION_NAME)) {
                logger.log(Logger.Level.WARN, "Vulkan 校验被要求启用，但当前平台不支持 VK_EXT_DEBUG_UTILS_EXTENSION_NAME 扩展");
                validation = false;
            }

            PointerBuffer requiredExtensionsBuf;
            if (validation) {
                requiredExtensionsBuf = stack.mallocPointer(glfwExtensionsBuf.remaining() + 1);
                requiredExtensionsBuf.put(glfwExtensionsBuf);
                requiredExtensionsBuf.put(stack.ASCII(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
                requiredExtensionsBuf.rewind();
            } else {
                requiredExtensionsBuf = glfwExtensionsBuf;
            }

            VkDebugUtilsMessengerCreateInfoEXT debugUtils = null;
            long pExtensions = MemoryUtil.NULL;
            if (validation) {
                debugUtils = createDebugCallback();
                pExtensions = debugUtils.address();
            }
            this.debugUtils = debugUtils;

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pNext(pExtensions)
                    .pApplicationInfo(appInfo)
                    .ppEnabledLayerNames(requiredLayersBuf)
                    .ppEnabledExtensionNames(requiredExtensionsBuf);

            PointerBuffer instanceBuf = stack.mallocPointer(1);
            int err = vkCreateInstance(createInfo, null, instanceBuf);
            if (err != VK_SUCCESS) {
                throw new RuntimeException(String.format("创建 Vulkan 实例失败，错误码：%d", err));
            }
            this.instance = new VkInstance(instanceBuf.get(0), createInfo);

            long debugHandle = VK_NULL_HANDLE;
            if (validation) {
                LongBuffer debugHandleBuf = stack.mallocLong(1);
                err = vkCreateDebugUtilsMessengerEXT(this.instance, debugUtils, null, debugHandleBuf);
                if (err != VK_SUCCESS) {
                    throw new RuntimeException(String.format("创建 Vulkan 调试回调失败，错误码：%d", err));
                }
                debugHandle = debugHandleBuf.get(0);
            }
            this.validation = validation;
            this.debugHandle = debugHandle;
        }
    }

    public boolean hasValidation() {
        return validation;
    }

    private Set<String> getSupportedValidationLayers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numLayersBuf = stack.callocInt(1);
            vkEnumerateInstanceLayerProperties(numLayersBuf, null);

            int numLayers = numLayersBuf.get(0);
            logger.log(Logger.Level.DEBUG, "实例支持 %d 个校验层", numLayers);

            VkLayerProperties.Buffer layerPropertiesBuf = VkLayerProperties.calloc(numLayers, stack);
            vkEnumerateInstanceLayerProperties(numLayersBuf, layerPropertiesBuf);

            Set<String> supportedValidationLayers = layerPropertiesBuf.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(Collectors.toSet());
            for (String layer : supportedValidationLayers) {
                logger.log(Logger.Level.DEBUG, "实例支持校验层: %s", layer);
            }
            return supportedValidationLayers;
        }
    }

    private Set<String> getInstanceExtensions() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numExtensionsBuf = stack.callocInt(1);
            vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, null);

            int numExtensions = numExtensionsBuf.get(0);
            logger.log(Logger.Level.DEBUG, "实例支持 %d 个扩展", numExtensions);

            VkExtensionProperties.Buffer extensionPropertiesBuf = VkExtensionProperties.calloc(numExtensions, stack);
            vkEnumerateInstanceExtensionProperties((String) null, numExtensionsBuf, extensionPropertiesBuf);

            Set<String> instanceExtensions = extensionPropertiesBuf.stream()
                    .map(VkExtensionProperties::extensionNameString)
                    .collect(Collectors.toSet());
            for (String extension : instanceExtensions) {
                logger.log(Logger.Level.DEBUG, "实例支持扩展: %s", extension);
            }
            return instanceExtensions;
        }
    }

    private static final int MESSAGE_TYPE_BITMASK = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

    private static VkDebugUtilsMessengerCreateInfoEXT createDebugCallback() {
        int messageSeverityBitmask = calculateMessageSeverityBitmask();
        //noinspection resource
        return VkDebugUtilsMessengerCreateInfoEXT.calloc()
                .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(messageSeverityBitmask)
                .messageType(MESSAGE_TYPE_BITMASK)
                .pfnUserCallback((severity, messageTypes, pCallbackData, pUserData) -> {
                    VkDebugUtilsMessengerCallbackDataEXT callbackData =
                            VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);
                    Logger.Level level = switch (severity) {
                        case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT -> Logger.Level.DEBUG;
                        case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT -> Logger.Level.INFO;
                        case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> Logger.Level.WARN;
                        case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> Logger.Level.ERROR;
                        default -> Logger.Level.FATAL;
                    };

                    logger.log(level, callbackData.pMessageString());
                    return VK_FALSE;
                });
    }

    private static int calculateMessageSeverityBitmask() {
        int logLevel = Logger.getLevel().getValue();

        int messageSeverityBitmask = 0;
        if (logLevel <= Logger.Level.DEBUG.getValue()) {
            messageSeverityBitmask |= VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
        }
        if (logLevel <= Logger.Level.INFO.getValue()) {
            messageSeverityBitmask |= VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT;
        }
        if (logLevel <= Logger.Level.WARN.getValue()) {
            messageSeverityBitmask |= VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
        }
        if (logLevel <= Logger.Level.ERROR.getValue()) {
            messageSeverityBitmask |= VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
        }
        return messageSeverityBitmask;
    }

    private final VkInstance instance;
    private final long debugHandle;
    private final VkDebugUtilsMessengerCreateInfoEXT debugUtils;
    private final boolean validation;

    private static final Logger logger = new Logger(Instance.class.getName());

    @Override
    public void close() {
        if (debugHandle != 0) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugHandle, null);
        }

        if (debugUtils != null) {
            debugUtils.pfnUserCallback().free();
            debugUtils.free();
        }

        vkDestroyInstance(instance, null);
        logger.log(Logger.Level.INFO, "成功销毁了 Vulkan 实例");
    }
}
