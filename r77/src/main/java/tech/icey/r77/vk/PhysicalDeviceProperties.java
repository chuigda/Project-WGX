package tech.icey.r77.vk;

import tech.icey.util.Optional;

import java.util.List;

public record PhysicalDeviceProperties(
        long deviceId,
        String deviceName,
        long driverVersion,
        long vendorId,
        PhysicalDeviceType deviceType,
        List<String> deviceExtensions,
        Optional<Long> graphicsQueueFamilyIndex,
        Optional<Long> presentationQueueFamilyIndex
) {
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
