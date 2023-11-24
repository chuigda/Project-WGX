package test.gui;

import test.gui.utils.JFrameHelper;

import javax.swing.*;
import java.awt.*;

public class JointAnimationForm extends WGXBaseForm {
    private JPanel basePanel;
    private JRadioButton alwaysOnRadioButton;
    private JRadioButton blinkRadioButton;
    private JRadioButton offRadioButton;
    private JSlider timerSlider;
    private JButton a0Button;
    private JButton a1Button;
    private JButton aButton;
    private JButton bButton;
    private JButton resetButton;

    public JointAnimationForm() {
        super(I18n.tr("jointanimform.title"));
        ButtonGroup modeRadioGroup = new ButtonGroup();
        modeRadioGroup.add(alwaysOnRadioButton);
        modeRadioGroup.add(blinkRadioButton);
        modeRadioGroup.add(offRadioButton);
        modeRadioGroup.setSelected(alwaysOnRadioButton.getModel(), true);
    }

    public static JointAnimationForm initialize(Component parent, Runnable onFinish) {
        JointAnimationForm ja = new JointAnimationForm();
        JFrameHelper.initializeForm(ja, parent, onFinish);
        return ja;
    }

    @Override
    public JPanel getBasePanel() {
        return basePanel;
    }
}
