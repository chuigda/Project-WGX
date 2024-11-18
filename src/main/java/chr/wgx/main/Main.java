package chr.wgx.main;

import chr.wgx.ui.ControlWindow;

import java.util.logging.Logger;

public final class Main {
    public static void main(String[] args) {
        ControlWindow controlWindow = new ControlWindow();
        controlWindow.setVisible(true);

        logger.info("应用程序已启动");
        Bootload.loadNativeLibraries();
        logger.info("本地库已加载完成");
    }

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tFT%1$tT] [%4$s] %3$s : %5$s%n");
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());
}
