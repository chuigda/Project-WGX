package test.gui;

import tech.icey.util.Pair;
import test.gui.utils.LinkRunner;
import test.gui.utils.SysInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;

public class AttitudeCtrl {
    private static JFrame frame = null;

    private JPanel basePanel;
    private JRadioButton manualRadioButton;
    private JRadioButton osfRadioButton;
    private JRadioButton vtsRadioButton;
    private JLabel helpMessageLinkLabel;
    private JPanel helpMessagePanel;

    public AttitudeCtrl() {
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
            throw new RuntimeException(e);
        }
        helpMessageLinkLabel.setText("<html><a href=\"%s\">%s</a></html>".formatted(
                link,
                I18n.tr("attictrlpane.btn.helpMessage.vts.linkText")
        ));
        helpMessageLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpMessageLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LinkRunner(uri).execute();
            }
        });
    }

    public static Pair<JFrame, AttitudeCtrl> initialize(Component parent, Runnable onFinish) {
        JFrame frame = new JFrame(I18n.tr("attictrlpane.title"));
        AttitudeCtrl ac = new AttitudeCtrl();
        frame.setContentPane(ac.basePanel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onFinish.run();
            }
        });
        return new Pair<>(frame, ac);
    }
}
