package tech.icey.wgx;

import tech.icey.util.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public final class ControlWindow extends JFrame {
    public ControlWindow() {
        super("Project-WGX - 控制器");
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon-v2.png")));
        this.setIconImage(icon.getImage());

        this.menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        this.systemMenu = new JMenu("System");
        this.menuBar.add(this.systemMenu);

        this.exitMenuItem = new JMenuItem("Exit");
        this.systemMenu.add(this.exitMenuItem);

        this.textArea = new JTextArea();
        Font font = new Font("SimSun", Font.PLAIN, 10);
        if (!font.canDisplay('中')) {
            font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        }
        this.textArea.setFont(font);
        this.textArea.setLineWrap(true);
        this.textArea.setEditable(false);

        this.scrollPane = new JScrollPane(this.textArea);
        this.add(this.scrollPane);

        this.setSize(480, 360);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addLogText(@NotNull String logText) {
        if (this.textArea.getLineCount() > 3000) {
            try {
                int start = this.textArea.getLineStartOffset(0);
                int end = this.textArea.getLineEndOffset(100);
                this.textArea.replaceRange("", start, end);
            } catch (Exception ignored) {}
        }

        this.textArea.append(logText);
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }

    private final JMenuBar menuBar;
    private final JMenu systemMenu;
    private final JMenuItem exitMenuItem;

    private final JTextArea textArea;
    private final JScrollPane scrollPane;
}
