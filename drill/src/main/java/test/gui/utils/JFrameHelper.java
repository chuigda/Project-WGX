package test.gui.utils;

import test.gui.WGXBaseForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class JFrameHelper {
    public static void initializeForm(WGXBaseForm base, Component parent, Runnable onFinish) {
        base.setContentPane(base.getBasePanel());
        base.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        base.pack();
        base.setLocationRelativeTo(parent);
        base.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onFinish.run();
            }
        });
    }
}
