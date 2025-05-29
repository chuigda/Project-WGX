package chr.wgx.ui.config;

import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.annotation.unsigned;
import club.doki7.ffm.buffer.IntBuffer;
import club.doki7.vulkan.Constants;
import club.doki7.vulkan.Version;
import club.doki7.vulkan.VulkanLoader;
import club.doki7.vulkan.bitmask.VkQueueFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.command.EntryCommands;
import club.doki7.vulkan.command.InstanceCommands;
import club.doki7.vulkan.command.StaticCommands;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.VkPhysicalDeviceType;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vulkan.handle.VkInstance;
import club.doki7.vulkan.handle.VkPhysicalDevice;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;

public final class VulkanDeviceInfo {
    public final @Unsigned int deviceId;
    public final @EnumType(VkPhysicalDeviceType.class) int deviceType;
    public final String deviceName;
    public final Version.Decoded apiVersion;
    public final boolean supportsMSAA;
    public final List<Integer> msaaSampleCounts;
    public final float maxAnisotropy;
    public final boolean dedicatedTransferQueue;

    public VulkanDeviceInfo(
            @Unsigned int deviceId,
            @EnumType(VkPhysicalDeviceType.class) int deviceType,
            String deviceName,
            Version.Decoded apiVersion,
            boolean supportsMSAA,
            List<Integer> msaaSampleCounts,
            float maxAnisotropy,
            boolean dedicatedTransferQueue
    ) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.deviceName = deviceName;
        this.apiVersion = apiVersion;
        this.supportsMSAA = supportsMSAA;
        this.msaaSampleCounts = msaaSampleCounts;
        this.maxAnisotropy = maxAnisotropy;
        this.dedicatedTransferQueue = dedicatedTransferQueue;
    }

    public static List<VulkanDeviceInfo> listVulkanDevices() {
        StaticCommands sCmd;
        EntryCommands eCmd;
        try {
            VulkanLoader.loadVulkanLibrary();
            sCmd = VulkanLoader.loadStaticCommands();
            eCmd = VulkanLoader.loadEntryCommands();
        } catch (Throwable e) {
            // 说明找不到 Vulkan 驱动
            return List.of();
        }

        VkInstance instance = null;
        InstanceCommands iCmd = null;
        try (Arena arena = Arena.ofConfined()) {
            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.allocate(arena);
            VkInstance.Buffer pInstance = VkInstance.Buffer.allocate(arena);
            int result = eCmd.vkCreateInstance(createInfo, null, pInstance);
            if (result != VkResult.VK_SUCCESS) {
                return List.of();
            }
            instance = pInstance.read();
            iCmd = VulkanLoader.loadInstanceCommands(instance, sCmd);

            IntBuffer pDeviceCount = IntBuffer.allocate(arena);
            result = iCmd.vkEnumeratePhysicalDevices(instance, pDeviceCount, null);
            if (result != VkResult.VK_SUCCESS) {
                return List.of();
            }

            int deviceCount = pDeviceCount.read();
            if (deviceCount == 0) {
                return List.of();
            }

            VkPhysicalDevice.Buffer pPhysicalDevices = VkPhysicalDevice.Buffer.allocate(arena, deviceCount);
            result = iCmd.vkEnumeratePhysicalDevices(instance, pDeviceCount, pPhysicalDevices);
            if (result != VkResult.VK_SUCCESS) {
                return List.of();
            }
            VkPhysicalDevice[] physicalDevices = pPhysicalDevices.readAll();

            List<VulkanDeviceInfo> devices = new ArrayList<>();
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.allocate(arena);
            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.allocate(arena);
            for (int i = 0; i < deviceCount; i++) {
                VkPhysicalDevice physicalDevice = physicalDevices[i];
                iCmd.vkGetPhysicalDeviceProperties(physicalDevice, properties);
                iCmd.vkGetPhysicalDeviceFeatures(physicalDevice, features);

                if (properties.apiVersion() < Version.VK_API_VERSION_1_3) {
                    continue;
                }

                VkPhysicalDeviceLimits limits = properties.limits();

                @Unsigned int deviceId = properties.deviceID();
                @EnumType(VkPhysicalDeviceType.class) int deviceType = properties.deviceType();
                String deviceName = properties.deviceName().readString();

                boolean supportsMSAA = features.sampleRateShading() == VkConstants.TRUE;
                List<Integer> msaaSampleCounts = sampleCountBitsToSampleCountList(
                        limits.framebufferColorSampleCounts() & limits.framebufferDepthSampleCounts()
                );
                boolean supportsAnisotropy = features.samplerAnisotropy() == VkConstants.TRUE;
                float maxAnisotropy = supportsAnisotropy ? limits.maxSamplerAnisotropy() : 1.0f;

                boolean dedicatedTransferQueue = false;
                // get all queue families
                IntBuffer pQueueFamilyCount = IntBuffer.allocate(arena);
                iCmd.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyCount, null);
                int queueFamilyCount = pQueueFamilyCount.read();
                VkQueueFamilyProperties[] queueFamilyProperties = VkQueueFamilyProperties.allocate(arena, queueFamilyCount);
                iCmd.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyCount, queueFamilyProperties[0]);

                for (int j = 0; j < queueFamilyCount; j++) {
                    VkQueueFamilyProperties queueFamilyProperty = queueFamilyProperties[j];
                    if ((queueFamilyProperty.queueFlags() & VkQueueFlags.VK_QUEUE_TRANSFER_BIT) != 0 &&
                            (queueFamilyProperty.queueFlags() & VkQueueFlags.VK_QUEUE_GRAPHICS_BIT) == 0 &&
                            (queueFamilyProperty.queueFlags() & VkQueueFlags.VK_QUEUE_COMPUTE_BIT) == 0) {
                        dedicatedTransferQueue = true;
                        break;
                    }
                }

                devices.add(new VulkanDeviceInfo(
                        deviceId,
                        deviceType,
                        deviceName,
                        Version.decode(properties.apiVersion()),
                        supportsMSAA,
                        msaaSampleCounts,
                        maxAnisotropy,
                        dedicatedTransferQueue
                ));
            }
            return devices;
        } catch (Throwable e) {
            return List.of();
        } finally {
            try {
                if (instance != null && iCmd != null) {
                    iCmd.vkDestroyInstance(instance, null);
                }
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    private static List<Integer> sampleCountBitsToSampleCountList(
            @EnumType(VkSampleCountFlags.class) int sampleCountBits
    ) {
        List<Integer> sampleCounts = new ArrayList<>();
        if ((sampleCountBits & VkSampleCountFlags._1) != 0) {
            sampleCounts.add(1);
        }
        if ((sampleCountBits & VkSampleCountFlags.VK_SAMPLE_COUNT_2_BIT) != 0) {
            sampleCounts.add(2);
        }
        if ((sampleCountBits & VkSampleCountFlags.VK_SAMPLE_COUNT_4_BIT) != 0) {
            sampleCounts.add(4);
        }
        if ((sampleCountBits & VkSampleCountFlags.VK_SAMPLE_COUNT_8_BIT) != 0) {
            sampleCounts.add(8);
        }
        if ((sampleCountBits & VkSampleCountFlags.VK_SAMPLE_COUNT_16_BIT) != 0) {
            sampleCounts.add(16);
        }
        if ((sampleCountBits & VkSampleCountFlags.VK_SAMPLE_COUNT_32_BIT) != 0) {
            sampleCounts.add(32);
        }
        if ((sampleCountBits & VkSampleCountFlags.VK_SAMPLE_COUNT_64_BIT) != 0) {
            sampleCounts.add(64);
        }
        return sampleCounts;
    }
}
