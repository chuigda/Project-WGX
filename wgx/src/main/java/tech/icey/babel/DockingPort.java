package tech.icey.babel;

import javax.swing.*;

public interface DockingPort {
    void addElement(String name, long location, JPanel panel);
}
