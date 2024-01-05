package tech.icey.r77.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import tech.icey.util.Logger;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static tech.icey.util.RuntimeError.*;

public record PhysicalDevice(
		VkPhysicalDevice vkPhysicalDevice,
		PhysicalDeviceProperties physicalDeviceProperties
) {
    public static List<PhysicalDevice> listPhysicalDevices(Instance instance) {
        VkInstance vkInstance = instance.vkInstance;

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

            List<PhysicalDevice> devices = new ArrayList<>();
            for (int i = 0; i < numDevices; i++) {
                try (MemoryStack stack1 = MemoryStack.stackPush()) {
                    VkPhysicalDevice vkPhysicalDevice = new VkPhysicalDevice(devicesBuf.get(i), vkInstance);
                    VkPhysicalDeviceProperties vkPhysicalDeviceProperties = VkPhysicalDeviceProperties.calloc(stack1);
                    vkGetPhysicalDeviceProperties(vkPhysicalDevice, vkPhysicalDeviceProperties);

                    String deviceName = vkPhysicalDeviceProperties.deviceNameString();
                    long deviceId = Integer.toUnsignedLong(vkPhysicalDeviceProperties.deviceID());
                    long driverVersion = Integer.toUnsignedLong(vkPhysicalDeviceProperties.driverVersion());
                    long vendorId = Integer.toUnsignedLong(vkPhysicalDeviceProperties.vendorID());

                    IntBuffer numDeviceExtensionsBuf = stack1.mallocInt(1);
                    ret = vkEnumerateDeviceExtensionProperties(
                            vkPhysicalDevice,
                            (String) null,
                            numDeviceExtensionsBuf,
                            null
                    );
                    if (ret != VK_SUCCESS) {
                        runtimeError("枚举物理设备扩展失败: %d", ret);
                    }

                    int numDeviceExtensions = numDeviceExtensionsBuf.get(0);
                    VkExtensionProperties.Buffer extensionsBuf = VkExtensionProperties.calloc(numDeviceExtensions, stack1);
                    ret = vkEnumerateDeviceExtensionProperties(
                            vkPhysicalDevice,
                            (String) null,
                            numDeviceExtensionsBuf,
                            extensionsBuf
                    );
                    if (ret != VK_SUCCESS) {
                        runtimeError("枚举物理设备扩展失败: %d", ret);
                    }

                    List<String> deviceExtensions = new ArrayList<>();
                    for (int j = 0; j < numDeviceExtensions; j++) {
                        deviceExtensions.add(extensionsBuf.get(j).extensionNameString());
                    }

                    IntBuffer numQueueFamilyPropertiesBuf = stack1.mallocInt(1);
                    vkGetPhysicalDeviceQueueFamilyProperties(
                            vkPhysicalDevice,
                            numQueueFamilyPropertiesBuf,
                            null
                    );
                    int numQueueFamilyProperties = numQueueFamilyPropertiesBuf.get(0);

                    VkQueueFamilyProperties.Buffer queueFamilyPropertiesBuf =
                            VkQueueFamilyProperties.calloc(numQueueFamilyProperties, stack1);
                    vkGetPhysicalDeviceQueueFamilyProperties(
                            vkPhysicalDevice,
                            numQueueFamilyPropertiesBuf,
                            queueFamilyPropertiesBuf
                    );

                    List<VkQueueFamilyProperties> graphicsQueueFamilies = new ArrayList<>();
                    for (int j = 0; j < numQueueFamilyProperties; j++) {
                        VkQueueFamilyProperties queueFamilyProperties = queueFamilyPropertiesBuf.get(j);
                        if ((queueFamilyProperties.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                        	graphicsQueueFamilies.add(queueFamilyProperties);
                        }
                    }

                    devices.add(new PhysicalDevice(vkPhysicalDevice, new PhysicalDeviceProperties(
	                            deviceId,
	                            deviceName,
	                            driverVersion,
	                            vendorId,
	                            switch (vkPhysicalDeviceProperties.deviceType()) {
	                                case VK_PHYSICAL_DEVICE_TYPE_OTHER -> 
	                                	PhysicalDeviceProperties.PhysicalDeviceType.OTHER;
	                                case VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU ->
	                                	PhysicalDeviceProperties.PhysicalDeviceType.INTEGRATED_GPU;
	                                case VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU ->
	                                	PhysicalDeviceProperties.PhysicalDeviceType.DISCRETE_GPU;
	                                case VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU ->
	                                	PhysicalDeviceProperties.PhysicalDeviceType.VIRTUAL_GPU;
	                                case VK_PHYSICAL_DEVICE_TYPE_CPU ->
	                                	PhysicalDeviceProperties.PhysicalDeviceType.CPU;
	                                default -> unreachable();
	                            },
	                            deviceExtensions,
	                            graphicsQueueFamilies
                    )));
                }
            }

            return devices;
        }
    }
    
    private static final Logger logger = new Logger(PhysicalDevice.class.getName());
}
