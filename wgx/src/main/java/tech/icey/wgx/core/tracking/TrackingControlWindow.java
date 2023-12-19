package tech.icey.wgx.core.tracking;

import tech.icey.wgx.babel.DockingPort;

import javax.swing.*;

public final class TrackingControlWindow extends JFrame implements DockingPort {
    public TrackingControlWindow() {
        super("姿态控制");
        this.setSize(400, 400);
    }

    @Override
    public void addElement(String name, long location, JPanel panel) {
        // TODO
    }
}
