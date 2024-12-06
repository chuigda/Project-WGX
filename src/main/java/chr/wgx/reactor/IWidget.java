package chr.wgx.reactor;

import javax.swing.*;

public interface IWidget {
    String displayName();
    JPanel getContentPanel();

    void onDock();
    void onUndock();
}
