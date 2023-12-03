package tech.icey.wgx;

import tech.icey.r77.Init;
import tech.icey.r77.vk.Instance;
import tech.icey.r77.vk.VkWindow;
import tech.icey.util.IniParser;
import tech.icey.util.Logger;
import tech.icey.util.Pair;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var controlWindow = new ControlWindow();
        controlWindow.setVisible(true);

        Date startTime = new Date();
        SwingUtilities.invokeLater(() -> controlWindow.addLogText(String.format(
                "T=    +0.000 (%s) PROGRAM STARTUP\n",
                String.format("%tFT%<tT.%<tL%<tz", startTime)
        )));

        Logger.setLevel(Logger.Level.WARN);
        Logger.installHook((time, level, message) -> {
            int milliSecondsElapsed = (int) (time.getTime() - startTime.getTime());
            float secondsElapsed = milliSecondsElapsed / 1000f;
            SwingUtilities.invokeLater(() -> controlWindow.addLogText(
                    String.format("T=%+10.3f %s %s\n", secondsElapsed, level.name(), message)
            ));
            return null;
        });

        try {
            Config config = readConfig();
            Logger.Level dedicatedLevel = Logger.Level.fromString(config.logLevel);
            if (dedicatedLevel == null) {
                logger.log(Logger.Level.WARN, "配置文件中的日志级别无效, 将会回退为默认值 (WARN)");
            } else {
                Logger.setLevel(dedicatedLevel);
                logger.log(Logger.Level.INFO, "日志级别已设置为 %s", dedicatedLevel.name());
            }

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

    private static Config readConfig() {
        try {
            String iniContent = Files.readString(Paths.get("config.txt"));
            Pair<HashMap<String, HashMap<String, String>>, List<String>> parseResult = IniParser.parse(iniContent);
            if (!parseResult.second().isEmpty()) {
                logger.log(Logger.Level.WARN, "配置文件解析时发生了以下错误:");
                for (String error : parseResult.second()) {
                    logger.log(Logger.Level.WARN, "\t%s", error);
                }
                logger.log(
                        Logger.Level.WARN,
                        "存在语法错误的 ini 行将会被忽略, 程序会继续运行, 但我们建议您检查配置文件"
                );
            }

            HashMap<String, HashMap<String, String>> ini = parseResult.first();
            Pair<Config, List<String>> deserialiseResult = IniParser.deserialise(Config.class, ini);

            if (!deserialiseResult.second().isEmpty()) {
                logger.log(Logger.Level.WARN, "配置文件反序列化时发生了以下错误:");
                for (String error : deserialiseResult.second()) {
                    logger.log(Logger.Level.WARN, "\t%s", error);
                }
                logger.log(
                        Logger.Level.WARN,
                        "存在错误的 ini 行将会被忽略, 程序会继续运行, 但我们建议您检查配置文件"
                );
            }

            return deserialiseResult.first();
        } catch (IOException e) {
            logger.log(Logger.Level.WARN, "读取配置文件失败: %s, 将会回退为默认配置", e.getMessage());
            return new Config();
        }
    }

    private static void renderMain() {
        Init.initialise();

        try (var window = new VkWindow("你好", 600, 600);
             var instance = new Instance("Project-WGX", true)) {
            while (window.poll()) {}
        }
    }

    private static final Logger logger = new Logger(Main.class.getName());
}
