package tech.icey.wgx;

import tech.icey.r77.Init;
import tech.icey.r77.vk.Instance;
import tech.icey.r77.vk.PhysicalDevice;
import tech.icey.r77.vk.PhysicalDeviceProperties;
import tech.icey.r77.vk.VkWindow;
import tech.icey.util.IniParser;
import tech.icey.util.Logger;
import tech.icey.util.Pair;
import tech.icey.util.Optional;
import tech.icey.wgx.babel.BabelPlugin;
import tech.icey.wgx.babel.UIProvider;
import tech.icey.wgx.core.ExampleKotlinPlugin;
import tech.icey.wgx.core.MatrixGeneratorPlugin;
import tech.icey.wgx.core.editor.EditorPlugin;
import tech.icey.wgx.core.tracking.TrackingPlugin;
import tech.icey.wgx.ui.ControlWindow;
import tech.icey.wgx.ui.DeviceInfoDialog;
import tech.icey.wgx.ui.UICommonUtils;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logger.log(Logger.Level.FATAL, "线程 %s 发生了一个无法恢复的异常: %s", t.getName(), e.getMessage());
            logger.log(Logger.Level.FATAL, "堆栈跟踪: ");
            for (StackTraceElement element : e.getStackTrace()) {
                logger.log(Logger.Level.FATAL, "\t%s", element.toString());
            }
        });

        MetalLookAndFeel.setCurrentTheme(UICommonUtils.defaultMetalTheme);
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            logger.log(Logger.Level.WARN, "设置外观时发生了错误: %s", e.getMessage());
        }

        var controlWindow = new ControlWindow();
        controlWindow.setVisible(true);

        Date startTime = new Date();
        SwingUtilities.invokeLater(() -> controlWindow.addLogText(String.format(
                "T=+0.000 (%s) IGNITION\n",
                String.format("%tFT%<tT.%<tL%<tz", startTime)
        )));

        Logger.setLevel(Logger.Level.WARN);
        Logger.installHook((time, level, message) -> {
            int milliSecondsElapsed = (int) (time.getTime() - startTime.getTime());
            float secondsElapsed = milliSecondsElapsed / 1000f;
            SwingUtilities.invokeLater(() -> controlWindow.addLogText(
                    String.format("T=%+.3f %s %s\n", secondsElapsed, level.name(), message)
            ));
            return null;
        });

        try {
            Config config = readConfig();
            Logger.setLogStderrAlways(config.logStderrAlways);
            Optional<Logger.Level> dedicatedLevel = Logger.Level.fromString(config.logLevel);
            if (dedicatedLevel instanceof Optional.Some<Logger.Level> someDedicatedLevel) {
                Logger.setLevel(someDedicatedLevel.value);
                logger.log(Logger.Level.INFO, "日志级别已设置为 %s", someDedicatedLevel.value.name());
            } else {
                logger.log(Logger.Level.WARN, "配置文件中的日志级别无效, 将会回退为默认值 (WARN)");
            }

            List<BabelPlugin> plugins = List.of(
                    new TrackingPlugin(),
                    new EditorPlugin(),
                    new MatrixGeneratorPlugin(),
                    new ExampleKotlinPlugin()
            );
            List<List<Object>> pluginComponents = plugins.stream().map(BabelPlugin::getComponents).toList();
            List<UIProvider> uiProviders = pluginComponents.stream()
                    .flatMap(List::stream)
                    .filter(component -> component instanceof UIProvider)
                    .map(component -> (UIProvider) component)
                    .toList();

            SwingUtilities.invokeLater(() -> {
                controlWindow.installPluginDatabase(plugins, pluginComponents);
                controlWindow.installPluginUI(uiProviders);
            });

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
            errorMessageBuilder.append("如果你确信这是一个程序 bug，可向 Project-WGX 的开发者报告:\n");
            errorMessageBuilder.append("\thttps://github.com/chuigda/Project-WGX/issues");

            logger.log(Logger.Level.FATAL, errorMessageBuilder.toString());
        }
    }

    private static Config readConfig() {
        try {
            String iniContent = Files.readString(Paths.get("config.txt"));
            Pair<HashMap<String, HashMap<String, String>>, List<String>> parseResult = IniParser.parse(iniContent);
            if (!parseResult.second.isEmpty()) {
                logger.log(Logger.Level.WARN, "配置文件解析时发生了以下错误:");
                for (String error : parseResult.second) {
                    logger.log(Logger.Level.WARN, "\t%s", error);
                }
                logger.log(
                        Logger.Level.WARN,
                        "存在语法错误的 ini 行将会被忽略, 程序会继续运行, 但我们建议您检查配置文件"
                );
            }

            HashMap<String, HashMap<String, String>> ini = parseResult.first;
            Pair<Config, List<String>> deserialiseResult = IniParser.deserialise(Config.class, ini);

            if (!deserialiseResult.second.isEmpty()) {
                logger.log(Logger.Level.WARN, "配置文件反序列化时发生了以下错误:");
                for (String error : deserialiseResult.second) {
                    logger.log(Logger.Level.WARN, "\t%s", error);
                }
                logger.log(
                        Logger.Level.WARN,
                        "存在错误的 ini 行将会被忽略, 程序会继续运行, 但我们建议您检查配置文件"
                );
            }

            return deserialiseResult.first;
        } catch (IOException e) {
            logger.log(Logger.Level.WARN, "读取配置文件失败: %s, 将会回退为默认配置", e.getMessage());
            return new Config();
        }
    }

    private static void renderMain() {
        Init.initialise();

        try (var window = new VkWindow("你好", 600, 600);
             var instance = new Instance("Project-WGX", true)) {
        	List<PhysicalDevice> physicalDevices = PhysicalDevice.listPhysicalDevices(instance);
        	List<PhysicalDeviceProperties> physicalDeviceProperties = physicalDevices.stream()
        			.map(PhysicalDevice::physicalDeviceProperties)
        			.toList();
        	
        	DeviceInfoDialog deviceInfoDialog = new DeviceInfoDialog(physicalDeviceProperties, null);
        	deviceInfoDialog.setVisible(true);
        	
            while (window.poll()) {}
        }
    }

    private static final Logger logger = new Logger(Main.class.getName());
}
