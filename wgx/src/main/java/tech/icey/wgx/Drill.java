package tech.icey.wgx;

import javax.swing.WindowConstants;

import tech.icey.wgx.core.editor.SimpleEditor;
import tech.icey.wgx.core.tracking.TrackingControlWindow;

public class Drill {
    public static void main(String[] args) {
       TrackingControlWindow w = new TrackingControlWindow(e -> { return null; }, e -> { return null; });
       w.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
       w.setVisible(true);
    }
}
