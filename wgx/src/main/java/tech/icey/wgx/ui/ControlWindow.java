package tech.icey.wgx.ui;

import tech.icey.util.Optional;
import tech.icey.wgx.babel.BabelPlugin;
import tech.icey.wgx.babel.DockingPort;
import tech.icey.wgx.babel.UIComponent;
import tech.icey.wgx.babel.UIProvider;
import tech.icey.util.Logger;
import tech.icey.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

        this.systemMenu = new JMenu("系统");
        menuBar.add(systemMenu);

        JMenuItem pluginManagementItem = new JMenuItem("插件管理");
        systemMenu.add(pluginManagementItem);
        pluginManagementItem.addActionListener(e -> {
            if (pluginWindow instanceof Optional.Some<PluginWindow> somePluginWindow) {
                somePluginWindow.value.setVisible(!somePluginWindow.value.isVisible());
            } else {
                // plugin systems not loaded yet, prompt user for that
                JOptionPane.showMessageDialog(
                        this,
                        "插件系统尚未加载，请稍等片刻",
                        "提示",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

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

        this.helpMenu = new JMenu("帮助");
        menuBar.add(helpMenu);
        JMenuItem helpMenuItem = new JMenuItem("帮助主题");
        helpMenu.add(helpMenuItem);
        JMenuItem aboutItem = new JMenuItem("关于");
        helpMenu.add(aboutItem);

        this.pluginWindow = Optional.none();

        JPanel contentPanel = new JPanel();
        GridBagLayout contentPanelLayout = new GridBagLayout();
        contentPanel.setLayout(contentPanelLayout);
        this.setContentPane(contentPanel);

        this.textArea = new JTextArea();
        Font font = UICommonUtils.defaultMonospaceFont.deriveFont(10.0f);
        this.textArea.setFont(font);
        this.textArea.setLineWrap(true);
        this.textArea.setEditable(false);
        UICommonUtils.createTextAreaMenu(this.textArea);

        JScrollPane scrollPane = new JScrollPane(this.textArea);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        this.add(scrollPane, c);

        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusLabel.setFont(font);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        c.gridy = 1;
        this.add(statusLabel, c);

        this.setMinimumSize(new Dimension(480, 360));
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // update indicator every 100ms
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    Runtime runtime = Runtime.getRuntime();
                    long totalMemory = runtime.totalMemory() / (1024 * 1024);
                    long freeMemory = runtime.freeMemory() / (1024 * 1024);
                    long maxMemory = runtime.maxMemory() / (1024 * 1024);
                    long usedMemory = totalMemory - freeMemory;
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText(String.format(
                                "JVM 内存: 当前 %d, 最大 %d, 已使用 %d, 空闲 %d (MiB)",
                                totalMemory,
                                maxMemory,
                                usedMemory,
                                freeMemory
                        ));
                    });
                } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    public void installPluginDatabase(List<BabelPlugin> plugins, List<List<Object>> pluginComponents) {
        logger.info("加载插件信息数据库");
        this.pluginWindow = Optional.some(new PluginWindow(plugins, pluginComponents));
    }

    public void installPluginUI(List<UIProvider> uiProviderList) {
        logger.info("正在安装插件 UI");

        menuBar.removeAll();
        menuBar.add(this.systemMenu);

        HashMap<String, UIComponent> components = new HashMap<>();
        HashMap<String, JMenu> createdMenus = new HashMap<>();

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
                menuItemComponent.frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        menuItem.setSelected(false);
                    }
                });

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

        menuBar.add(this.helpMenu);
        this.revalidate();
    }

    public void addLogText(String logText) {
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
    private final JMenu systemMenu;
    private final JMenu helpMenu;
    private Optional<PluginWindow> pluginWindow;

    private final JTextArea textArea;
    private boolean logPaused = false;
    private final Logger logger = new Logger(ControlWindow.class.getName());
}
