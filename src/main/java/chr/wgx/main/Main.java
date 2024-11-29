package chr.wgx.main;

import chr.wgx.Config;
import chr.wgx.ui.ControlWindow;
import chr.wgx.ui.LicenseWindow;
import chr.wgx.util.JULUtil;
import chr.wgx.util.ResourceUtil;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        checkLicense();

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

        logger.info(String.format("配置文件内容:\n%s", config.toPrettyJSON()));

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

    private static void checkLicense() {
        Path agreementFilePath = Path.of("LICENSE.AGREED");
        if (Files.exists(agreementFilePath)) {
            return;
        }

        try {
            String agplBrief = ResourceUtil.readTextFile("/resources/license/brief/LICENSE-AGPLv3.txt");
            String ccBrief = ResourceUtil.readTextFile("/resources/license/brief/LICENSE-CC-BY-SA-4.0.txt");
            String agplFullText = ResourceUtil.readTextFile("/resources/license/LICENSE-AGPLv3.txt");
            String ccFullText = ResourceUtil.readTextFile("/resources/license/LICENSE-CC-BY-SA-4.0.txt");

            LicenseWindow w = new LicenseWindow(
                    List.of(
                            new LicenseWindow.License("GNU Affero General Public License v3.0", agplBrief, agplFullText),
                            new LicenseWindow.License("Creative Commons Attribution-ShareAlike 4.0 International", ccBrief, ccFullText)
                    )
            );
            if (!w.requireAgreement()) {
                System.exit(0);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "检测到非法的修改，程序即将退出", "错误", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        try {
            Files.createFile(agreementFilePath);
        } catch (IOException e) {
            // ignore the error, this is not a fatal error anyway
        }
    }

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tFT%1$tT] [%4$s] %3$s : %5$s%n");
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());
}
