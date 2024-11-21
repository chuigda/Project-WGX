package chr.wgx.main;

import chr.wgx.ui.ControlWindow;

import java.util.logging.Logger;

public final class Main {
    public static void main(String[] args) {
        ControlWindow controlWindow = new ControlWindow();
        controlWindow.setVisible(true);

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
