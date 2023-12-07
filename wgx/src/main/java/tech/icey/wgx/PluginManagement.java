package tech.icey.wgx;

import tech.icey.babel.BabelPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class PluginManagement extends JFrame {
    public PluginManagement(List<BabelPlugin> plugins) {
        super("插件管理");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 4));

        JList<String> pluginList = new JList<>(plugins.stream().map(BabelPlugin::getName).toArray(String[]::new));
        JScrollPane pluginListScrollPane = new JScrollPane(pluginList);
        pluginListScrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JPanel pluginListPanel = new JPanel();
        pluginListPanel.setBorder(BorderFactory.createTitledBorder("插件列表"));
        pluginListPanel.setLayout(new BorderLayout());
        pluginListPanel.add(pluginListScrollPane, BorderLayout.CENTER);

        JLabel pluginDescription = new JLabel("选择一个插件来查看详情");
        JScrollPane pluginDescriptionScrollPane = new JScrollPane(pluginDescription);
        // scroll only vertically
        pluginDescriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pluginDescriptionScrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        JPanel pluginDetailPanel = new JPanel();
        pluginDetailPanel.setBorder(BorderFactory.createTitledBorder("插件描述"));
        pluginDetailPanel.setLayout(new BorderLayout());
        pluginDetailPanel.add(pluginDescriptionScrollPane, BorderLayout.CENTER);

        JList<String> pluginComponentList = new JList<>();
        JScrollPane pluginComponentListScrollPane = new JScrollPane(pluginComponentList);
        pluginComponentListScrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JPanel pluginComponentListPanel = new JPanel();
        pluginComponentListPanel.setBorder(BorderFactory.createTitledBorder("插件提供的组件"));
        pluginComponentListPanel.setLayout(new BorderLayout());
        pluginComponentListPanel.add(pluginComponentListScrollPane, BorderLayout.CENTER);

        JPanel componentBehaviourPanel = new JPanel();
        componentBehaviourPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("组件行为"),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        componentBehaviourPanel.setLayout(new BoxLayout(componentBehaviourPanel, BoxLayout.Y_AXIS));

        pluginList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int selectedIndex = pluginList.getSelectedIndex();
            if (selectedIndex == -1) {
                return;
            }
            BabelPlugin selectedPlugin = plugins.get(selectedIndex);
            pluginDescription.setText(selectedPlugin.getDescription());
            pluginComponentList.setListData(selectedPlugin.getComponents().stream().map(Object::toString).toArray(String[]::new));
        });

        panel.add(pluginListPanel);
        panel.add(pluginDetailPanel);
        panel.add(pluginComponentListPanel);
        panel.add(componentBehaviourPanel);

        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.setContentPane(panel);
        this.setSize(1000, 320);
        this.setResizable(false);
    }
}
