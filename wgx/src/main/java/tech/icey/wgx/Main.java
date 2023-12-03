package tech.icey.wgx;

import tech.icey.r77.Init;
import tech.icey.r77.vk.Instance;
import tech.icey.r77.vk.VkWindow;
import tech.icey.util.Logger;

import javax.swing.*;

import static tech.icey.util.RuntimeError.runtimeError;

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

        try (var window = new VkWindow("你好", 600, 600)) {
            Instance instance = new Instance("Project-WGX", true);

            while (window.poll()) { runtimeError("测试异常处理器"); }
        } catch (Exception e) {
            logger.log(Logger.Level.FATAL, "发生了一个无法恢复的异常: %s，程序必须中止", e.getMessage());
            logger.log(Logger.Level.FATAL, "堆栈跟踪: ");
            for (StackTraceElement element : e.getStackTrace()) {
                logger.log(Logger.Level.FATAL, "\t - %s", element.toString());
            }
            logger.log(Logger.Level.FATAL, "如果你确信这是一个程序 bug，可向 Project-WGX 的开发者报告:");
            logger.log(Logger.Level.FATAL, "\thttps://github.com/chuigda/Project-WGX/issues");
        }
    }

    private static Logger logger = new Logger(Main.class.getName());
}
