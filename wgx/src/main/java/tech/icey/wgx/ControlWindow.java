package tech.icey.wgx;

import tech.icey.babel.DockingPort;
import tech.icey.babel.HasMenuBar;
import tech.icey.babel.UIEntryPoint;
import tech.icey.babel.UIProvider;
import tech.icey.kit.FontDatabase;
import tech.icey.kit.MenuFactory;
import tech.icey.util.Logger;
import tech.icey.util.NotNull;
import tech.icey.util.Pair;
import tech.icey.util.Tuple3;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;

public final class ControlWindow extends JFrame {
    public ControlWindow(List<UIProvider> uiProviderList) {
        super("Project-WGX - 控制器");
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon-v2.png")));
        this.setIconImage(icon.getImage());

        this.menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        this.createdMenus = new HashMap<>();

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

        installUI(uiProviderList);

        JMenu helpMenu = new JMenu("帮助");
        menuBar.add(helpMenu);
        JMenuItem helpMenuItem = new JMenuItem("帮助主题");
        helpMenu.add(helpMenuItem);
        JMenuItem aboutItem = new JMenuItem("关于");
        helpMenu.add(aboutItem);

        this.textArea = new JTextArea();
        Font font = FontDatabase.defaultMonospaceFont.deriveFont(10.0f);
        this.textArea.setFont(font);
        this.textArea.setLineWrap(true);
        this.textArea.setEditable(false);
        MenuFactory.createTextAreaMenu(this.textArea);

        JScrollPane scrollPane = new JScrollPane(this.textArea);
        this.add(scrollPane);

        this.setSize(480, 360);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void installUI(List<UIProvider> uiProviderList) {
        HashMap<String, Pair<UIEntryPoint, JPanel>> components = new HashMap<>();

        for (UIProvider provider : uiProviderList) {
            for (Tuple3<String, UIEntryPoint, JPanel> uiItem : provider.provide()) {
                String name = uiItem.first;
                UIEntryPoint entryPoint = uiItem.second;
                JPanel panel = uiItem.third;

                components.put(name, new Pair<>(entryPoint, panel));
            }
        }

        for (Pair<UIEntryPoint, JPanel> uiItem : components.values()) {
            UIEntryPoint entryPoint = uiItem.first;
            JPanel panel = uiItem.second;

            if (entryPoint instanceof UIEntryPoint.MenuItem menuItemEntryPoint) {
                if (!createdMenus.containsKey(menuItemEntryPoint.menuName)) {
                    JMenu menu = new JMenu(menuItemEntryPoint.menuName);
                    menuBar.add(menu);
                    createdMenus.put(menuItemEntryPoint.menuName, menu);
                }

                JMenu targetMenu = createdMenus.get(menuItemEntryPoint.menuName);
                JMenuItem menuItem = new JMenuItem(menuItemEntryPoint.menuItemName);
                JFrame containingFrame = new JFrame(menuItemEntryPoint.popupFrameName);
                containingFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                containingFrame.setContentPane(panel);

                if (panel instanceof HasMenuBar hasMenuBar) {
                    JMenuBar menuBar = hasMenuBar.getMenuBar();
                    containingFrame.setJMenuBar(menuBar);
                }

                containingFrame.pack();
                menuItem.addActionListener(e -> {
                    containingFrame.setVisible(true);
                    containingFrame.setLocationRelativeTo(null);
                });

                targetMenu.add(menuItem);
            } else if (entryPoint instanceof UIEntryPoint.SubElement subElementEntryPoint) {
                String targetName = subElementEntryPoint.targetName;
                if (!components.containsKey(targetName)) {
                    logger.log(Logger.Level.WARN, "目标元素不存在: %s", targetName);
                }

                Pair<UIEntryPoint, JPanel> target = components.get(targetName);
                JPanel targetPanel = target.second;
                if (targetPanel instanceof DockingPort dockingPort) {
                    dockingPort.addElement(subElementEntryPoint.name, subElementEntryPoint.location, panel);
                } else {
                    logger.log(
                            Logger.Level.WARN,
                            "目标元素 (具有类型 %s) 不是 DockingPort: %s",
                            targetPanel.getClass().getName(),
                            targetName
                    );
                }
            }
        }
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

    private final JMenuBar menuBar;
    private final HashMap<String, JMenu> createdMenus;
    private final JTextArea textArea;
    private boolean logPaused = false;
    private final Logger logger = new Logger(ControlWindow.class.getName());
}
