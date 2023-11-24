package test.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

public class CtrlPane {
    private AttitudeCtrlForm attitudeCtrlForm;

    private JointAnimationForm jointAnimationForm;

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
        attitudeCtrlForm = AttitudeCtrlForm.initialize(basePanel, () -> attiCtrlToggleButton.setSelected(false));
        jointAnimationForm = JointAnimationForm.initialize(basePanel, () -> jointAnimToggleButton.setSelected(false));

        addTglBtnListener(attiCtrlToggleButton, attitudeCtrlForm);
        addTglBtnListener(jointAnimToggleButton, jointAnimationForm);
    }

    public static void show() {
        JFrame frame = new JFrame(I18n.tr("ctrlpane.title"));
        frame.setContentPane(new CtrlPane().basePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null); // center window
        frame.setVisible(true);
    }

    private static void addTglBtnListener(JToggleButton btn, WGXBaseForm form) {
        btn.addActionListener((ActionEvent e) -> {
            EventQueue.invokeLater(() -> {
                if (!btn.isSelected()) {
                    form.dispatchEvent(new WindowEvent(form, WindowEvent.WINDOW_CLOSING));
                } else {
                    form.setVisible(true);
                }
            });
        });
    }
}
