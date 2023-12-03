package tech.icey.r77.vk;

import tech.icey.util.NotNull;

import java.util.List;
import java.util.Optional;

public record PhysicalDeviceProperties(
        long deviceId,
        @NotNull String deviceName,
        long driverVersion,
        long vendorId,
        @NotNull PhysicalDeviceType deviceType,
        @NotNull List<String> deviceExtensions,
        Optional<Long> graphicsQueueFamilyIndex,
        Optional<Long> presentationQueueFamilyIndex
) {
    enum PhysicalDeviceType {
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
