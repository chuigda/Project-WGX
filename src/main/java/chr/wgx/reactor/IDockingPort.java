package chr.wgx.reactor;

import javax.swing.*;

public interface IDockingPort {
    default String getPortName() {
        return this.getClass().getName();
    }

    boolean addElement(String name, long location, JPanel contentPanel);
}
