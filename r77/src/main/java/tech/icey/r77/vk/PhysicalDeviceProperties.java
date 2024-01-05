package tech.icey.r77.vk;

import java.util.List;

import org.lwjgl.vulkan.VkQueueFamilyProperties;

public final class PhysicalDeviceProperties {
    public PhysicalDeviceProperties(
            long deviceId,
            String deviceName,
            long driverVersion,
            long vendorId,
            PhysicalDeviceType deviceType,
            List<String> deviceExtensions,
            List<VkQueueFamilyProperties> graphicsQueueFamilies
    ) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.driverVersion = driverVersion;
        this.vendorId = vendorId;
        this.deviceType = deviceType;
        this.deviceExtensions = deviceExtensions;
        this.graphicsQueueFamilies = graphicsQueueFamilies;
    }

    public final long deviceId;
    public final String deviceName;
    public final long driverVersion;
    public final long vendorId;
    public final PhysicalDeviceType deviceType;
    public final List<String> deviceExtensions;
    public final List<VkQueueFamilyProperties> graphicsQueueFamilies;
    public enum PhysicalDeviceType {
        OTHER,
        INTEGRATED_GPU,
        DISCRETE_GPU,
        VIRTUAL_GPU,
        CPU;

        public String descriptiveName() {
            return switch (this) {
                case OTHER -> "其他";
                case INTEGRATED_GPU -> "集成 GPU";
                case DISCRETE_GPU -> "独立 GPU";
                case VIRTUAL_GPU -> "虚拟 GPU";
                case CPU -> "CPU";
            };
        }
    }
}
