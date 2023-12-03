package tech.icey.wgx;

import tech.icey.util.IniField;
import tech.icey.util.NotNull;

public final class Config {
    @IniField(key = "log")
    @NotNull public String logLevel = "warn";

    @IniField(section = "vulkan", key = "validation")
    public boolean vulkanValidation = false;
    @IniField(section = "vulkan", key = "device-uuid")
    public String vulkanDeviceUUID = "";

    @IniField(section = "test", key = "test")
    public int testValue = 0;
}
