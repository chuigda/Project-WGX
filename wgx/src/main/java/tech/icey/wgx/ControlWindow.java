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

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu systemMenu = new JMenu("系统");
        menuBar.add(systemMenu);

        JMenu logLevelSubMenu = new JMenu("日志级别");
        systemMenu.add(logLevelSubMenu);

        JMenuItem logLevelDebugMenuItem = new JMenuItem("调试");
        logLevelSubMenu.add(logLevelDebugMenuItem);
        JMenuItem logLevelInfoMenuItem = new JMenuItem("信息");
        logLevelSubMenu.add(logLevelInfoMenuItem);
        JMenuItem logLevelWarnMenuItem = new JMenuItem("警告");
        logLevelSubMenu.add(logLevelWarnMenuItem);
        JMenuItem logLevelErrorMenuItem = new JMenuItem("错误");
        logLevelSubMenu.add(logLevelErrorMenuItem);

        JMenuItem pauseOrResumeMenuItem = new JMenuItem("暂停日志") {
            @Override
            public void doClick(int pressTime) {
                logPaused = !logPaused;
                this.setText(logPaused ? "恢复日志" : "暂停日志");
            }
        };
        systemMenu.add(pauseOrResumeMenuItem);

        JMenuItem exitMenuItem = new JMenuItem("退出") {
            @Override
            public void doClick(int pressTime) {
                System.exit(0);
            }
        };
        systemMenu.add(exitMenuItem);

        JMenu helpMenu = new JMenu("帮助");
        menuBar.add(helpMenu);

        JMenuItem helpMenuItem = new JMenuItem("帮助主题");
        helpMenu.add(helpMenuItem);
        JMenuItem aboutItem = new JMenuItem("关于");
        helpMenu.add(aboutItem);

        this.textArea = new JTextArea();
        Font font = new Font("SimSun", Font.PLAIN, 10);
        if (!font.canDisplay('中')) {
            font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        }
        this.textArea.setFont(font);
        this.textArea.setLineWrap(true);
        this.textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(this.textArea);
        this.add(scrollPane);

        this.setSize(480, 360);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addLogText(@NotNull String logText) {
        if (this.logPaused) {
            return;
        }

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

    private final JTextArea textArea;
    private boolean logPaused = false;
}
