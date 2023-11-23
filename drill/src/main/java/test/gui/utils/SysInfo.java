package test.gui.utils;

import java.awt.*;

public abstract class SysInfo {
    public static boolean isBrowserSupported() {
        return Desktop.isDesktopSupported() || Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }
}
