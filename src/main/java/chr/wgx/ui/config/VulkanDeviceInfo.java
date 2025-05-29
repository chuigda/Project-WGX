package chr.wgx.ui.config;

import club.doki7.ffm.annotation.EnumType;
import club.doki7.ffm.annotation.Unsigned;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.Version;
import club.doki7.vulkan.bitmask.VkQueueFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.command.VkEntryCommands;
import club.doki7.vulkan.command.VkInstanceCommands;
import club.doki7.vulkan.command.VkStaticCommands;
import club.doki7.vulkan.command.VulkanLoader;
import club.doki7.vulkan.datatype.*;
import club.doki7.vulkan.enumtype.VkPhysicalDeviceType;
import club.doki7.vulkan.enumtype.VkResult;
import club.doki7.vulkan.handle.VkInstance;
import club.doki7.vulkan.handle.VkPhysicalDevice;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VulkanDeviceInfo {
    public final @Unsigned int deviceId;
    public final @EnumType(VkPhysicalDeviceType.class) int deviceType;
    public final String deviceName;
    public final Version apiVersion;
    public final boolean supportsMSAA;
    public final List<Integer> msaaSampleCounts;
    public final float maxAnisotropy;
    public final boolean dedicatedTransferQueue;

    public VulkanDeviceInfo(
            @Unsigned int deviceId,
            @EnumType(VkPhysicalDeviceType.class) int deviceType,
            String deviceName,
            Version apiVersion,
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
        VkStaticCommands sCmd;
        VkEntryCommands eCmd;
        try {
            VulkanLoader.loadVulkanLibrary();
            sCmd = VulkanLoader.loadStaticCommands();
            eCmd = VulkanLoader.loadEntryCommands(sCmd);
        } catch (Throwable e) {
            // 说明找不到 Vulkan 驱动
            return List.of();
        }

        @Nullable VkInstance instance = null;
        @Nullable VkInstanceCommands iCmd = null;
        try (Arena arena = Arena.ofConfined()) {
            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.allocate(arena);
            VkInstance.Ptr pInstance = VkInstance.Ptr.allocate(arena);
            int result = eCmd.createInstance(createInfo, null, pInstance);
            if (result != VkResult.SUCCESS) {
                return List.of();
            }
            instance = Objects.requireNonNull(pInstance.read());
            iCmd = VulkanLoader.loadInstanceCommands(instance, sCmd);

            IntPtr pDeviceCount = IntPtr.allocate(arena);
            result = iCmd.enumeratePhysicalDevices(instance, pDeviceCount, null);
            if (result != VkResult.SUCCESS) {
                return List.of();
            }

            int deviceCount = pDeviceCount.read();
            if (deviceCount == 0) {
                return List.of();
            }

            VkPhysicalDevice.Ptr pPhysicalDevices = VkPhysicalDevice.Ptr.allocate(arena, deviceCount);
            result = iCmd.enumeratePhysicalDevices(instance, pDeviceCount, pPhysicalDevices);
            if (result != VkResult.SUCCESS) {
                return List.of();
            }

            List<VulkanDeviceInfo> devices = new ArrayList<>();
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.allocate(arena);
            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.allocate(arena);
            for (int i = 0; i < deviceCount; i++) {
                VkPhysicalDevice physicalDevice = Objects.requireNonNull(pPhysicalDevices.read(i));
                iCmd.getPhysicalDeviceProperties(physicalDevice, properties);
                iCmd.getPhysicalDeviceFeatures(physicalDevice, features);

                if (properties.apiVersion() < Version.VK_API_VERSION_1_3.encode()) {
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
                IntPtr pQueueFamilyCount = IntPtr.allocate(arena);
                iCmd.getPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyCount, null);
                int queueFamilyCount = pQueueFamilyCount.read();
                VkQueueFamilyProperties.Ptr queueFamilyProperties = VkQueueFamilyProperties.allocate(arena, queueFamilyCount);
                iCmd.getPhysicalDeviceQueueFamilyProperties(physicalDevice, pQueueFamilyCount, queueFamilyProperties);

                for (int j = 0; j < queueFamilyCount; j++) {
                    VkQueueFamilyProperties queueFamilyProperty = queueFamilyProperties.at(i);
                    if ((queueFamilyProperty.queueFlags() & VkQueueFlags.TRANSFER) != 0 &&
                            (queueFamilyProperty.queueFlags() & VkQueueFlags.GRAPHICS) == 0 &&
                            (queueFamilyProperty.queueFlags() & VkQueueFlags.COMPUTE) == 0) {
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
                    iCmd.destroyInstance(instance, null);
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
        if ((sampleCountBits & VkSampleCountFlags._2) != 0) {
            sampleCounts.add(2);
        }
        if ((sampleCountBits & VkSampleCountFlags._4) != 0) {
            sampleCounts.add(4);
        }
        if ((sampleCountBits & VkSampleCountFlags._8) != 0) {
            sampleCounts.add(8);
        }
        if ((sampleCountBits & VkSampleCountFlags._16) != 0) {
            sampleCounts.add(16);
        }
        if ((sampleCountBits & VkSampleCountFlags._32) != 0) {
            sampleCounts.add(32);
        }
        if ((sampleCountBits & VkSampleCountFlags._64) != 0) {
            sampleCounts.add(64);
        }
        return sampleCounts;
    }
}
