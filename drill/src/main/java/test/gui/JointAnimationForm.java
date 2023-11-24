package test.gui;

import test.gui.utils.JFrameHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class JointAnimationForm extends WGXBaseForm {
    private static final int ANIM_LIST_NUM_BTNS_PER_ROW = 5;

    private JPanel basePanel;
    private JRadioButton alwaysOnRadioButton;
    private JRadioButton blinkRadioButton;
    private JRadioButton offRadioButton;
    private JSlider timerSlider;
    private JButton aButton;
    private JButton bButton;
    private JButton resetButton;
    private JPanel animListBtnPanel;

    public JointAnimationForm() {
        super(I18n.tr("jointanimform.title"));
        var numBtns = 50;
        var layout = new GridLayout();
        layout.setHgap(5);
        layout.setVgap(5);
        layout.setColumns(ANIM_LIST_NUM_BTNS_PER_ROW);
        layout.setRows(numBtns / ANIM_LIST_NUM_BTNS_PER_ROW);
        animListBtnPanel.setLayout(layout);

        ButtonGroup modeRadioGroup = new ButtonGroup();
        modeRadioGroup.add(alwaysOnRadioButton);
        modeRadioGroup.add(blinkRadioButton);
        modeRadioGroup.add(offRadioButton);
        modeRadioGroup.setSelected(alwaysOnRadioButton.getModel(), true);

        for (int i = 0; i < numBtns; i++) {
            var btn = new JButton(Integer.toString(i));
            btn.addActionListener((ActionEvent e) -> System.out.println(e.paramString()));
            animListBtnPanel.add(btn);
        }
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
