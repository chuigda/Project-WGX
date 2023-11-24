package test.gui;

import test.gui.attictrl.VTSSettingForm;
import test.gui.utils.JFrameHelper;

import javax.swing.*;
import java.awt.*;

public class AttitudeCtrlForm extends WGXBaseForm {
    private VTSSettingForm vtsSettingForm = new VTSSettingForm();

    private JPanel basePanel;
    private JRadioButton manualRadioButton;
    private JRadioButton osfRadioButton;
    private JRadioButton vtsRadioButton;
    private JPanel detailsPanel;

    public AttitudeCtrlForm() {
        super(I18n.tr("attictrlform.title"));
        ButtonGroup modeRadioGroup = new ButtonGroup();
        modeRadioGroup.add(manualRadioButton);
        modeRadioGroup.add(osfRadioButton);
        modeRadioGroup.add(vtsRadioButton);

        manualRadioButton.addActionListener((e) -> {
            detailsPanel.removeAll();
            this.setPreferredSize(null);
            detailsPanel.revalidate();
            System.out.println(e.paramString());
        });
        osfRadioButton.addActionListener((e) -> {
            detailsPanel.removeAll();
            this.setPreferredSize(null);
            detailsPanel.revalidate();
            System.out.println(e.paramString());
        });
        vtsRadioButton.addActionListener((e) -> {
            detailsPanel.removeAll();
            this.setPreferredSize(null);
            detailsPanel.add(vtsSettingForm.getBasePanel());
            detailsPanel.revalidate();
        });

        vtsRadioButton.doClick();
    }

    public static AttitudeCtrlForm initialize(Component parent, Runnable onFinish) {
        AttitudeCtrlForm ac = new AttitudeCtrlForm();
        JFrameHelper.initializeForm(ac, parent, onFinish);
        return ac;
    }

    @Override
    public JPanel getBasePanel() {
        return basePanel;
    }
}
