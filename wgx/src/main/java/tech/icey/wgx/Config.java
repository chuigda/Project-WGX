package tech.icey.wgx;

import tech.icey.util.IniField;
import tech.icey.util.NotNull;
import tech.icey.util.Nullable;

public final class Config {
    @IniField(key = "log")
    @NotNull public String logLevel = "warn";

    @IniField(key = "log-file")
    @Nullable public String logFile = null;

    @IniField(key = "log-stderr-always")
    public boolean logStderrAlways = false;

    @IniField(section = "vulkan", key = "validation")
    public boolean vulkanValidation = false;
    @IniField(section = "vulkan", key = "device-uuid")
    public String vulkanDeviceUUID = "";
}
