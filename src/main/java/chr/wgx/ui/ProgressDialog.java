package chr.wgx.ui;

import javax.swing.*;
import java.awt.*;

public final class ProgressDialog extends JDialog {
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel label = new JLabel("进度");

    public ProgressDialog(JFrame parent, String title) {
        super(parent, title, false);

        setLayout(new GridBagLayout());
        progressBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        GridBagConstraints c =  new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        add(label, c);

        c.gridy = 1;
        add(progressBar, c);

        pack();

        Dimension preferredSize = getPreferredSize();
        setSize(400, preferredSize.height + 10);

        setResizable(false);
        setMinimumSize(getSize());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }

    public void setProgress(int progress, String text) {
        progressBar.setValue(progress);
        label.setText(text);
    }

    public void setProgress(String text) {
        label.setText(text);
    }
}
