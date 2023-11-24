package test.gui.attictrl;

import tech.icey.util.Unreachable;
import test.gui.I18n;
import test.gui.WGXBaseForm;
import test.gui.utils.LinkRunner;
import test.gui.utils.SysInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.URISyntaxException;

public class VTSSettingForm extends WGXBaseForm {
    private JPanel basePanel;
    private JPanel helpMessagePanel;
    private JLabel helpMessageLinkLabel;
    private JTextField webSocketPortInput;
    private JButton startButton;
    private JButton stopButton;

    public VTSSettingForm() {
        prepareHelpMessageLabels();
    }

    @Override
    public JPanel getBasePanel() {
        return basePanel;
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
                I18n.tr("attictrlform.vtsform.helpMessage.linkText")
        ));
        helpMessageLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        helpMessageLinkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LinkRunner(uri).execute();
            }
        });
    }
}
