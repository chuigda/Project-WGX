package tech.icey.wgx;

import tech.icey.r77.Init;
import tech.icey.r77.vk.Instance;
import tech.icey.r77.vk.PhysicalDevice;

public class Drill {
    public static void main(String[] args) {
        Init.initialise();
        try (var instance = new Instance("drill application", true)) {
            var physicalDeviceProperties = PhysicalDevice.listPhysicalDevices(instance, 0);
            DeviceInfoDialog w = new DeviceInfoDialog(physicalDeviceProperties, null);
            w.setVisible(true);
        }
    }
}
