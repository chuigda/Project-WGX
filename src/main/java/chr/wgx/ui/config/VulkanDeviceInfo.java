package chr.wgx.ui.config;

import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.annotation.unsigned;
import tech.icey.panama.buffer.IntBuffer;
import tech.icey.vk4j.Constants;
import tech.icey.vk4j.Version;
import tech.icey.vk4j.VulkanLoader;
import tech.icey.vk4j.bitmask.VkSampleCountFlags;
import tech.icey.vk4j.command.EntryCommands;
import tech.icey.vk4j.command.InstanceCommands;
import tech.icey.vk4j.command.StaticCommands;
import tech.icey.vk4j.datatype.VkInstanceCreateInfo;
import tech.icey.vk4j.datatype.VkPhysicalDeviceFeatures;
import tech.icey.vk4j.datatype.VkPhysicalDeviceLimits;
import tech.icey.vk4j.datatype.VkPhysicalDeviceProperties;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vk4j.handle.VkInstance;
import tech.icey.vk4j.handle.VkPhysicalDevice;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;

public final class VulkanDeviceInfo {
    public final @unsigned int deviceId;
    public final String deviceName;
    public final boolean supportsMSAA;
    public final List<Integer> msaaSampleCounts;
    public final float maxAnisotropy;

    public VulkanDeviceInfo(
            @unsigned int deviceId,
            String deviceName,
            boolean supportsMSAA,
            List<Integer> msaaSampleCounts,
            float maxAnisotropy
    ) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.supportsMSAA = supportsMSAA;
        this.msaaSampleCounts = msaaSampleCounts;
        this.maxAnisotropy = maxAnisotropy;
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

                @unsigned int deviceId = properties.deviceID();
                String deviceName = properties.deviceName().readString();

                boolean supportsMSAA = features.sampleRateShading() == Constants.VK_TRUE;
                List<Integer> msaaSampleCounts = sampleCountBitsToSampleCountList(
                        limits.framebufferColorSampleCounts() & limits.framebufferDepthSampleCounts()
                );
                boolean supportsAnisotropy = features.samplerAnisotropy() == Constants.VK_TRUE;
                float maxAnisotropy = supportsAnisotropy ? limits.maxSamplerAnisotropy() : 1.0f;

                devices.add(new VulkanDeviceInfo(
                        deviceId,
                        deviceName,
                        supportsMSAA,
                        msaaSampleCounts,
                        maxAnisotropy
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
            @enumtype(VkSampleCountFlags.class) int sampleCountBits
    ) {
        List<Integer> sampleCounts = new ArrayList<>();
        if ((sampleCountBits & VkSampleCountFlags.VK_SAMPLE_COUNT_1_BIT) != 0) {
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
