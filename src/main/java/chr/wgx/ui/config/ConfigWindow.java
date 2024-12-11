package chr.wgx.ui.config;

import chr.wgx.config.Config;
import chr.wgx.ui.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class ConfigWindow extends JFrame {
    public ConfigWindow(List<VulkanDeviceInfo> vulkanDeviceInfoList) {
        super("配置 Project-WGX");

        JMenu fileMenu = new JMenu("文件");
        JMenuItem loadFromConfig = new JMenuItem("读取 config.json");
        JMenuItem saveToConfig = new JMenuItem("保存 config.json");

        fileMenu.add(loadFromConfig);
        fileMenu.add(saveToConfig);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagLayout verticalLayout = new GridBagLayout();
        contentPanel.setLayout(verticalLayout);

        logLevelComboBox.setSelectedIndex(1);
        SwingUtil.addConfigItemFast(contentPanel, 0, "日志级别", logLevelComboBox);
        SwingUtil.addConfigItemFast(contentPanel, 1, "控制窗口宽度", controlWindowWidthField);
        SwingUtil.addConfigItemFast(contentPanel, 2, "控制窗口高度", controlWindowHeightField);
        SwingUtil.addConfigItemFast(contentPanel, 3, "渲染输出窗口标题", renderOutputWindowTitleField);
        SwingUtil.addConfigItemFast(contentPanel, 4, "渲染输出窗口宽度", renderOutputWindowWidthField);
        SwingUtil.addConfigItemFast(contentPanel, 5, "渲染输出窗口高度", renderOutputWindowHeightField);
        renderModeComboBox.setSelectedIndex(0);
        SwingUtil.addConfigItemFast(contentPanel, 6, "渲染模式", renderModeComboBox);

        vulkanConfigWidget = new VulkanConfigWidget(vulkanDeviceInfoList);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 7;
        c.gridwidth = 3;
        contentPanel.add(vulkanConfigWidget, c);

        this.setContentPane(contentPanel);
        this.pack();

        this.setMinimumSize(this.getPreferredSize());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private final JComboBox<String> logLevelComboBox = new JComboBox<>(new String[]{ "debug", "info", "warn", "error" });
    private final JTextField controlWindowWidthField = new JTextField(Integer.toString(Config.DEFAULT.controlWindowWidth));
    private final JTextField controlWindowHeightField = new JTextField(Integer.toString(Config.DEFAULT.controlWindowHeight));
    private final JTextField renderOutputWindowTitleField = new JTextField(Config.DEFAULT.windowTitle);
    private final JTextField renderOutputWindowWidthField = new JTextField(Integer.toString(Config.DEFAULT.windowWidth));
    private final JTextField renderOutputWindowHeightField = new JTextField(Integer.toString(Config.DEFAULT.windowHeight));
    private final JComboBox<String> renderModeComboBox = new JComboBox<>(new String[]{ "vulkan", "gles2" });
    private final VulkanConfigWidget vulkanConfigWidget;
}
