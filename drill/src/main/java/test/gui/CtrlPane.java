package test.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

public class CtrlPane {
    private AttitudeCtrl attitudeCtrl;
    private JFrame attitudeCtrlFrame;

    private JButton addOnsButton;
    private JPanel basePanel;
    private JButton screenButton;
    private JButton sndAnalButton;
    private JButton addOptsButton;
    private JButton helpButton;
    private JToggleButton openGLToggleButton;
    private JToggleButton objPosToggleButton;
    private JToggleButton attiCtrlToggleButton;
    private JToggleButton jointAnimToggleButton;

    public CtrlPane() {
        var attitudeCtrlInitRes = AttitudeCtrl.initialize(basePanel, () -> attiCtrlToggleButton.setSelected(false));
        attitudeCtrlFrame = attitudeCtrlInitRes.first();
        attitudeCtrl = attitudeCtrlInitRes.second();

        attiCtrlToggleButton.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(() -> {
                if (!attiCtrlToggleButton.isSelected()) {
                    attitudeCtrlFrame.dispatchEvent(new WindowEvent(attitudeCtrlFrame, WindowEvent.WINDOW_CLOSING));
                } else {
                    attitudeCtrlFrame.setVisible(true);
                }
            });
        });
    }

    public static void show() {
        JFrame frame = new JFrame(I18n.tr("ctrlpane.title"));
        frame.setContentPane(new CtrlPane().basePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null); // center window
        frame.setVisible(true);
    }
}
