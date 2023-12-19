package tech.icey.wgx.core.tracking;

import tech.icey.r77.math.Vector3;
import tech.icey.wgx.babel.DockingPort;

import javax.swing.*;
import java.util.function.Function;

public final class TrackingControlWindow extends JFrame implements DockingPort {
    public TrackingControlWindow(
            Function<Vector3, Void> setTranslationLimit,
            Function<Vector3, Void> setFaceAngleLimit
    ) {
        super("姿态控制");
        this.setSize(400, 400);
    }

    @Override
    public void addElement(String name, long location, JPanel panel) {
        // TODO
    }
}
