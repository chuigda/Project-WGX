package tech.icey.r77.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import tech.icey.util.Logger;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.*;

public class PhysicalDevice {
    public static List<PhysicalDeviceProperties> listPhysicalDevices(Instance instance) {
        VkInstance vkInstance = instance.getVkInstance();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer numDevicesBuf = stack.mallocInt(1);
            int ret = vkEnumeratePhysicalDevices(vkInstance, numDevicesBuf, null);
            if (ret != VK_SUCCESS) {
                runtimeError("枚举物理设备失败: %d", ret);
            }

            int numDevices = numDevicesBuf.get(0);
            logger.log(Logger.Level.DEBUG, "检测到 %d 个物理设备", numDevices);

            if (numDevices == 0) {
                return List.of();
            }

            PointerBuffer devicesBuf = stack.mallocPointer(numDevices);
            ret = vkEnumeratePhysicalDevices(vkInstance, numDevicesBuf, devicesBuf);
            if (ret != VK_SUCCESS) {
                runtimeError("枚举物理设备失败: %d", ret);
            }

            List<PhysicalDeviceProperties> devices = new ArrayList<>();
            for (int i = 0; i < numDevices; i++) {
                try (MemoryStack stack1 = MemoryStack.stackPush()) {
                    VkPhysicalDevice vkPhysicalDevice = new VkPhysicalDevice(devicesBuf.get(i), vkInstance);
                    VkPhysicalDeviceProperties vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.calloc(stack1);
                    vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties);

                    String deviceName = vkPhysicalDeviceProperties.deviceNameString();
                    long deviceId = Integer.toUnsignedLong(vkPhysicalDeviceProperties.deviceID());
                    long driverVersion = Integer.toUnsignedLong(vkPhysicalDeviceProperties.driverVersion());
                    long vendorId = Integer.toUnsignedLong(vkPhysicalDeviceProperties.vendorID());

                    IntBuffer numExtensionsBuf = stack1.mallocInt(1);
                    ret = vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String) null, numExtensionsBuf, null);
                    if (ret != VK_SUCCESS) {
                        runtimeError("枚举物理设备扩展失败: %d", ret);
                    }

                    int numExtensions = numDevicesBuf.get(0);
                    VkExtensionProperties.Buffer extensionsBuf = VkExtensionProperties.calloc(numExtensions, stack1);
                    ret = vkEnumerateDeviceExtensionProperties(vkPhysicalDevice, (String) null, numExtensionsBuf, extensionsBuf);
                    if (ret != VK_SUCCESS) {
                        runtimeError("枚举物理设备扩展失败: %d", ret);
                    }

                    List<String> supportedExtensions = new ArrayList<>();
                    for (int j = 0; j < numExtensions; j++) {
                        supportedExtensions.add(extensionsBuf.get(j).extensionNameString());
                    }
                }
            }
        }
    }

    private static final Logger logger = new Logger(PhysicalDevice.class.getName());
}
