package tech.icey.wgx;

import tech.icey.util.IniField;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Config {
    @IniField(key = "log")
    public String logLevel = "warn";

    @IniField(key = "log-file")
    public Optional<String> logFile = Optional.empty();

    @IniField(key = "log-stderr-always")
    public boolean logStderrAlways = false;

    @IniField(section = "vulkan", key = "validation")
    public boolean vulkanValidation = false;

    @IniField(section = "vulkan", key = "device-id")
    public Optional<Long> vulkanDeviceId = Optional.empty();
}
