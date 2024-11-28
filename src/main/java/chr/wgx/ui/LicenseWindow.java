package chr.wgx.ui;

import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.sync.Oneshot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public final class LicenseWindow extends JFrame {
    public static final class License {
        public final String licenseName;
        public final String brief;
        public final String fullText;

        public License(String licenseName, String brief, String fullText) {
            this.licenseName = licenseName;
            this.brief = brief;
            this.fullText = fullText;
        }
    }

    public LicenseWindow(List<License> licenses) {
        super("阅读协议");

        assert !licenses.isEmpty();
        this.licenses = licenses;

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        GridBagLayout verticalLayout = new GridBagLayout();
        contentPanel.setLayout(verticalLayout);

        GridBagConstraints c = new GridBagConstraints();

        JPanel labelAndButtonsPanel = new JPanel();
        BoxLayout horizLayout = new BoxLayout(labelAndButtonsPanel, BoxLayout.X_AXIS);
        labelAndButtonsPanel.setLayout(horizLayout);

        labelAndButtonsPanel.add(Box.createHorizontalStrut(2));
        licenseNameLabel = new JLabel("LICENSE-NAME");
        leftButton = new JButton("◀");
        rightButton = new JButton("▶");
        labelAndButtonsPanel.add(licenseNameLabel);
        labelAndButtonsPanel.add(Box.createHorizontalGlue());
        labelAndButtonsPanel.add(leftButton);
        labelAndButtonsPanel.add(rightButton);

        leftButton.addActionListener(_ -> prevLicense());

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        contentPanel.add(labelAndButtonsPanel, c);

        JPanel briefPanel = SwingUtil.createGroupBox("简介");
        briefPanel.setLayout(new BoxLayout(briefPanel, BoxLayout.Y_AXIS));
        briefTextArea = new JTextArea("ABCDE");
        briefTextArea.setLineWrap(true);
        briefTextArea.setEditable(false);
        briefTextArea.setBackground(Color.WHITE);
        SwingUtil.createTextAreaMenu(briefTextArea);
        JScrollPane briefScrollPane = new JScrollPane(briefTextArea);
        briefPanel.add(briefScrollPane);

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 1;
        contentPanel.add(briefPanel, c);

        JPanel fullPanel = SwingUtil.createGroupBox("全文");
        fullPanel.setLayout(new BoxLayout(fullPanel, BoxLayout.Y_AXIS));
        fullTextArea = new JTextArea("ANCDE");
        fullTextArea.setLineWrap(true);
        fullTextArea.setEditable(false);
        fullTextArea.setBackground(Color.WHITE);
        SwingUtil.createTextAreaMenu(fullTextArea);
        JScrollPane fullScrollPane = new JScrollPane(fullTextArea);
        fullPanel.add(fullScrollPane);

        c.gridy = 2;
        c.weighty = 2;
        contentPanel.add(fullPanel, c);

        this.setContentPane(contentPanel);
        this.pack();
        this.setSize(800, 1024);
        this.setResizable(false);
    }

    public boolean requireAgreement() {
        Pair<Oneshot.Sender<Boolean>, Oneshot.Receiver<Boolean>> channel = Oneshot.create();
        Oneshot.Sender<Boolean> sender = channel.first();
        agreementSender = Option.some(sender);

        Oneshot.Receiver<Boolean> receiver = channel.second();

        currentLicense = 0;

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sender.send(false);
            }
        });

        updateWidgets();

        setVisible(true);
        return receiver.recv();
    }

    public void showLicenses() {
        currentLicense = 0;
        agreementSender = Option.none();

        updateWidgets();
        setVisible(true);
    }

    private void updateWidgets() {
        licenseNameLabel.setText(licenses.get(currentLicense).licenseName);
        briefTextArea.setText(licenses.get(currentLicense).brief);
        fullTextArea.setText(licenses.get(currentLicense).fullText);

        leftButton.setEnabled(currentLicense != 0);

        if (currentLicense == licenses.size() - 1) {
            if (agreementSender.isSome()) {
                rightButton.setEnabled(true);
                rightButton.setText("✔");
                SwingUtil.removeAllActionListeners(rightButton);
                rightButton.addActionListener(_ -> submitAgreement());
            }
            else {
                rightButton.setEnabled(false);
            }
        }
        else {
            rightButton.setEnabled(true);
            rightButton.setText("▶");
            SwingUtil.removeAllActionListeners(rightButton);
            rightButton.addActionListener(_ -> nextLicense());
        }
    }

    private void prevLicense() {
        currentLicense--;
        updateWidgets();
    }

    private void nextLicense() {
        currentLicense++;
        updateWidgets();
    }

    private void submitAgreement() {
        agreementSender.get().send(true);
        dispose();
    }

    private final List<License> licenses;
    private final JLabel licenseNameLabel;
    private final JButton leftButton;
    private final JButton rightButton;
    private final JTextArea briefTextArea;
    private final JTextArea fullTextArea;

    private int currentLicense = 0;
    private Option<Oneshot.Sender<Boolean>> agreementSender = Option.none();
}
