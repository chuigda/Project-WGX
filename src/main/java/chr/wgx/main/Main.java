package chr.wgx.main;

import java.util.logging.Logger;

public final class Main {
    public static void main(String[] args) {
        logger.info("application started");
        Bootload.loadNativeLibraries();
        logger.info("native libraries loaded");
    }

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tFT%1$tT] [%4$-7s] %5$s %n");
    }

    private static final Logger logger = Logger.getLogger(Main.class.getName());
}
