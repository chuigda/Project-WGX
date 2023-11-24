package test.gui;

import tech.icey.util.Unreachable;
import test.gui.utils.JFrameHelper;
import test.gui.utils.LinkRunner;
import test.gui.utils.SysInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;

public class AttitudeCtrlForm extends WGXBaseForm {
    private JPanel basePanel;
    private JRadioButton manualRadioButton;
    private JRadioButton osfRadioButton;
    private JRadioButton vtsRadioButton;
    private JLabel helpMessageLinkLabel;
    private JPanel helpMessagePanel;

    public AttitudeCtrlForm() {
        super(I18n.tr("attictrlform.title"));
        ButtonGroup modeRadioGroup = new ButtonGroup();
        modeRadioGroup.add(manualRadioButton);
        modeRadioGroup.add(osfRadioButton);
        modeRadioGroup.add(vtsRadioButton);
        modeRadioGroup.setSelected(vtsRadioButton.getModel(), true);

        manualRadioButton.addActionListener((e) -> System.out.println(e.paramString()));
        osfRadioButton.addActionListener((e) -> System.out.println(e.paramString()));
        vtsRadioButton.addActionListener((e) -> System.out.println(e.paramString()));

        prepareHelpMessageLabels();
    }

    private void prepareHelpMessageLabels() {
        String link = "https://www.google.com";
        if (!SysInfo.isBrowserSupported()) {
            helpMessageLinkLabel.setText(link);
            return;
        }
        final URI uri;
        try {
            uri = new URI(link);
        } catch (URISyntaxException e) {
            throw new Unreachable("?");
        }
        helpMessageLinkLabel.setText("<html><a href=\"%s\">%s</a></html>".formatted(
                link,
                I18n.tr("attictrlform.btn.helpMessage.vts.linkText")
        ));
        helpMessageLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpMessageLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LinkRunner(uri).execute();
            }
        });
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
