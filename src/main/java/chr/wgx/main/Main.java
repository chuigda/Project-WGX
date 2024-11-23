package chr.wgx.main;

import chr.wgx.Config;
import chr.wgx.ui.ControlWindow;
import chr.wgx.util.JULUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatIntelliJLaf;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        ControlWindow controlWindow = new ControlWindow();
        controlWindow.setVisible(true);

        Config config = Config.config();
        controlWindow.setSize(config.controlWindowWidth, config.controlWindowHeight);

        JULUtil.setLogLevel(switch (config.logLevel.toLowerCase().trim()) {
            case "fine", "debug" -> Level.FINE;
            case "info" -> Level.INFO;
            case "warning" -> Level.WARNING;
            case "severe", "error" -> Level.SEVERE;
            default -> {
                logger.warning(String.format("未知的日志级别: %s, 使用默认级别 INFO", Config.config().logLevel));
                yield Level.INFO;
            }
        });

        try {
            ObjectMapper mapper = new ObjectMapper();
            String text = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
            logger.info(String.format("配置文件内容:\n%s", text));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // do nothing, we don't mind if the config file content was not printed
        }

        try {
            RenderApplication.applicationStart();
        }
        catch (Throwable e) {
            StringBuilder sb = new StringBuilder();
            sb.append("应用程序遇到致命错误:\n");
            sb.append(e.getClass().getCanonicalName())
                    .append(": ")
                    .append(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append("\n\tat ").append(ste.toString());
            }
            logger.severe(sb.toString());
        }
    }

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tFT%1$tT] [%4$s] %3$s : %5$s%n");
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());
}
