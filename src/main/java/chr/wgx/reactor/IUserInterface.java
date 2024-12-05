package chr.wgx.reactor;

import javax.swing.*;

public interface IUserInterface {
    default String qualName() {
        return this.getClass().getName();
    }

    JPanel getContentPanel();

    void onDock();
    void onUndock();
}
