package chr.wgx.main;

import chr.wgx.ui.config.ConfigWindow;
import chr.wgx.ui.config.VulkanDeviceInfo;
import com.formdev.flatlaf.FlatIntelliJLaf;

import java.util.List;

public final class Configurator {
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        List<VulkanDeviceInfo> deviceInfoList = VulkanDeviceInfo.listVulkanDevices();

        ConfigWindow w = new ConfigWindow();
        w.setVisible(true);
    }
}
