package tech.icey.wgx.babel;

import javax.swing.*;

public interface DockingPort {
    void addElement(String name, long location, JPanel panel);
}
