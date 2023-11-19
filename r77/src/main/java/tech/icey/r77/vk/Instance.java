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
                Logger.log(Logger.Level.WARN, "Vulkan 校验被要求启用，但当前平台不支持任何 Vulkan 校验层");
            }

            List<String> validationLayersUsed = new ArrayList<>();
            if (validationLayers.contains("VK_LAYER_KHRONOS_validation")) {
                validationLayersUsed.add("VK_LAYER_KHRONOS_validation");
                Logger.log(Logger.Level.INFO, "使用 VK_LAYER_KHRONOS_validation 校验层");
            } else if (validationLayers.contains("VK_LAYER_LUNARG_standard_validation")) {
                validationLayersUsed.add("VK_LAYER_LUNARG_standard_validation");
                Logger.log(Logger.Level.INFO, "使用 VK_LAYER_LUNARG_standard_validation 校验层（旧）");
            } else {
                Logger.log(Logger.Level.WARN, "Vulkan 校验被要求启用，但当前平台不支持任何 Vulkan 校验层");
            }

            PointerBuffer requiredLayers = null;
            if (!validationLayersUsed.isEmpty()) {
                requiredLayers = stack.mallocPointer(validationLayersUsed.size());
                for (String layer : validationLayersUsed) {
                    requiredLayers.put(stack.ASCII(layer));
                }
                requiredLayers.rewind();
            }

            PointerBuffer requiredExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
            long debugMessengerCreateInfo = MemoryUtil.NULL;
            if (requiredExtensions == null) {
                throw new RuntimeException("无法获取 GLFW 所需的 Vulkan 实例扩展");
            }
            requiredExtensions.rewind();

            if (requiredLayers != null) {
                requiredExtensions.put(stack.ASCII(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
                this.messengerCreateInfo = createMessengerCreateInfo();
                debugMessengerCreateInfo = messengerCreateInfo.address();
            } else {
                this.messengerCreateInfo = null;
            }

            VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                    .pNext(debugMessengerCreateInfo)
                    .pApplicationInfo(appInfo)
                    .ppEnabledLayerNames(requiredLayers)
                    .ppEnabledExtensionNames(requiredExtensions);

            PointerBuffer instance = stack.mallocPointer(1);
            int result = vkCreateInstance(instanceCreateInfo, null, instance);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("创建 Vulkan 实例失败，错误码：" + result);
            }

            this.instance = new VkInstance(instance.get(0), instanceCreateInfo);

            if (messengerCreateInfo != null) {
                LongBuffer debugMessenger = stack.longs(VK_NULL_HANDLE);
                System.err.println(debugMessenger.remaining());
                try {
                    result = vkCreateDebugUtilsMessengerEXT(this.instance, messengerCreateInfo, null, debugMessenger);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (result != VK_SUCCESS) {
                    Logger.log(Logger.Level.WARN, "创建 Vulkan 调试信使失败，错误码：" + result + "，调试功能可能会不可用");
                }

                this.debugHandle = debugMessenger.get(0);
            } else {
                this.debugHandle = 0;
            }
        }
    }

    private VkDebugUtilsMessengerCreateInfoEXT createMessengerCreateInfo() {
        int messageSeverityBitmask = calculateMessageSeverityBitmask();

        int messageTypeMask = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
                | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

        return VkDebugUtilsMessengerCreateInfoEXT
                .calloc()
                .sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT)
                .messageSeverity(messageSeverityBitmask)
                .messageType(messageTypeMask)
                .pfnUserCallback((messageSeverity, messageType, pCallbackData, pUserData) -> {
                    try (var callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData)) {
                        String message = callbackData.pMessageString();
                        switch (messageSeverity) {
                            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:
                                Logger.log(Logger.Level.DEBUG, message);
                                break;
                            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
                                Logger.log(Logger.Level.INFO, message);
                                break;
                            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
                                Logger.log(Logger.Level.ERROR, message);
                                break;
                            case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
                            default:
                                Logger.log(Logger.Level.WARN, message);
                                break;
                        }
                        return VK_FALSE;
                    }
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

    private Set<String> getSupportedValidationLayers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numLayers = stack.callocInt(1);
            vkEnumerateInstanceLayerProperties(numLayers, null);

            VkLayerProperties.Buffer properties = VkLayerProperties.calloc(numLayers.get(0), stack);
            vkEnumerateInstanceLayerProperties(numLayers, properties);

            return properties.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(Collectors.toSet());
        }
    }

    private Set<String> getInstanceExtensions() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numExtensions = stack.callocInt(1);
            vkEnumerateInstanceExtensionProperties((String) null, numExtensions, null);

            VkExtensionProperties.Buffer properties = VkExtensionProperties.calloc(numExtensions.get(0), stack);
            vkEnumerateInstanceExtensionProperties((String) null, numExtensions, properties);

            return properties.stream()
                    .map(VkExtensionProperties::extensionNameString)
                    .collect(Collectors.toSet());
        }
    }

    private final VkInstance instance;
    private final long debugHandle;
    private final VkDebugUtilsMessengerCreateInfoEXT messengerCreateInfo;

    @Override
    public void close() throws Exception {
        if (debugHandle != 0) {
            vkDestroyDebugUtilsMessengerEXT(instance, debugHandle, null);
        }
        if (messengerCreateInfo != null) {
            messengerCreateInfo.free();
        }
        vkDestroyInstance(instance, null);
    }
}
