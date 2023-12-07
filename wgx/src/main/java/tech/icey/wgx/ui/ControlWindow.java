package tech.icey.wgx.ui;

import tech.icey.wgx.babel.DockingPort;
import tech.icey.wgx.babel.UIComponent;
import tech.icey.wgx.babel.UIProvider;
import tech.icey.util.Logger;
import tech.icey.util.NotNull;
import tech.icey.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;

public final class ControlWindow extends JFrame {
    public ControlWindow() {
        super("Project-WGX - 控制器");
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon-v2.png")));
        this.setIconImage(icon.getImage());

        this.menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        this.createdMenus = new HashMap<>();

        JMenu systemMenu = new JMenu("系统");
        menuBar.add(systemMenu);

        JMenuItem pluginManagementItem = new JMenuItem("插件管理");
        systemMenu.add(pluginManagementItem);

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

    public void installPluginUI(List<UIProvider> uiProviderList) {
        HashMap<String, UIComponent> components = new HashMap<>();

        for (UIProvider provider : uiProviderList) {
            for (Pair<String, UIComponent> uiItem : provider.provide()) {
                String name = uiItem.first;
                UIComponent uiComponent = uiItem.second;

                components.put(name, uiComponent);
            }
        }

        for (UIComponent uiComponent : components.values()) {
            if (uiComponent instanceof UIComponent.MenuItem menuItemComponent) {
                if (!createdMenus.containsKey(menuItemComponent.menuName)) {
                    JMenu menu = new JMenu(menuItemComponent.menuName);
                    menuBar.add(menu);
                    createdMenus.put(menuItemComponent.menuName, menu);
                }

                JMenu targetMenu = createdMenus.get(menuItemComponent.menuName);
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(menuItemComponent.menuItemName);
                menuItem.addActionListener(e -> menuItemComponent.frame.setVisible(menuItem.isSelected()));

                targetMenu.add(menuItem);
            } else if (uiComponent instanceof UIComponent.SubElement subElementComponent) {
                String targetName = subElementComponent.targetName;
                if (!components.containsKey(targetName)) {
                    logger.log(Logger.Level.WARN, "目标元素不存在: %s", targetName);
                }

                UIComponent target = components.get(targetName);
                if (target.getUnderlyingItem() instanceof DockingPort dockingPort) {
                    dockingPort.addElement(
                            subElementComponent.name,
                            subElementComponent.location,
                            subElementComponent.panel
                    );
                } else {
                    logger.log(
                            Logger.Level.WARN,
                            "目标元素 %s (具有类型 %s) 不是 DockingPort",
                            targetName,
                            target.getUnderlyingItem().getClass().getName()
                    );
                }
            }
        }
        
        JMenu helpMenu = new JMenu("帮助");
        menuBar.add(helpMenu);
        JMenuItem helpMenuItem = new JMenuItem("帮助主题");
        helpMenu.add(helpMenuItem);
        JMenuItem aboutItem = new JMenuItem("关于");
        helpMenu.add(aboutItem);
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
