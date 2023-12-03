package tech.icey.wgx;

import tech.icey.r77.Init;
import tech.icey.r77.vk.Instance;
import tech.icey.r77.vk.VkWindow;
import tech.icey.util.Logger;

import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        var controlWindow = new ControlWindow();
        controlWindow.setVisible(true);

        Logger.setLevel(Logger.Level.DEBUG);
        Init.initialise();

        Logger.installHook((time, level, message) -> {
            String timeString = String.format("%tFT%<tT.%<tL%<tz", time);
            SwingUtilities.invokeLater(() -> controlWindow.addLogText(
                    String.format("%s %s %s\n", timeString, level.name(), message)
            ));
            return null;
        });

        Instance instance = new Instance("Project-WGX", true);

        try (var window = new VkWindow("你好", 600, 600)) {
            while (window.poll()) {}
        }
    }
}
