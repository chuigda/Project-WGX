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

        Logger.setLevel(Logger.Level.WARN);
        Logger.installHook((time, level, message) -> {
            var timeString = String.format("%tFT%<tT.%<tL%<tz", time);
            SwingUtilities.invokeLater(() -> controlWindow.addLogText(
                    String.format("%s %s %s\n", timeString, level.name(), message)
            ));
            return null;
        });

        try {
            renderMain();
        } catch (Exception e) {
            var errorMessageBuilder = new StringBuilder();
            errorMessageBuilder
                    .append("发生了一个无法恢复的异常: \n")
                    .append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n")
                    .append("堆栈跟踪: \n");
            for (StackTraceElement element : e.getStackTrace()) {
                errorMessageBuilder.append("\t").append(element.toString()).append("\n");
            }
            errorMessageBuilder.append("如果你确信这是一个程序 bug，可向 Project-WGX 的开发者报告:");
            errorMessageBuilder.append("\thttps://github.com/chuigda/Project-WGX/issues");

            logger.log(Logger.Level.FATAL, errorMessageBuilder.toString());
        }
    }

    private static void renderMain() {
        Init.initialise();

        try (var window = new VkWindow("你好", 600, 600);
             var instance = new Instance("Project-WGX", true)) {
            while (window.poll()) {}
        }
    }

    private static Logger logger = new Logger(Main.class.getName());
}
