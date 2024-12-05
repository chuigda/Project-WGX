package chr.wgx.reactor;

import javax.swing.*;

public interface IDockingPort {
    boolean addElement(String name, long location, JPanel contentPanel);
}
