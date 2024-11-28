package chr.wgx.main;

import chr.wgx.ui.config.ConfigWindow;
import com.formdev.flatlaf.FlatIntelliJLaf;

public final class Configurator {
    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        ConfigWindow w = new ConfigWindow();
        w.setVisible(true);
    }
}
