package tech.icey.wgx.ui;

import tech.icey.wgx.babel.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class PluginWindow extends JFrame {
    public PluginWindow(List<BabelPlugin> plugins, List<List<Object>> pluginComponents) {
        super("插件管理");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));

        JList<String> pluginList = new JList<>(plugins.stream().map(BabelPlugin::getName).toArray(String[]::new));
        JScrollPane pluginListScrollPane = new JScrollPane(pluginList);
        pluginListScrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JPanel pluginListPanel = new JPanel();
        pluginListPanel.setBorder(BorderFactory.createTitledBorder("插件列表"));
        pluginListPanel.setLayout(new BorderLayout());
        pluginListPanel.add(pluginListScrollPane, BorderLayout.CENTER);

        JTextArea pluginDescription = new JTextArea("选择一个插件来查看详情");
        JScrollPane pluginDescriptionScrollPane = new JScrollPane(pluginDescription);
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
        JCheckBox dataConsumerCheckBox = new JCheckBox("DataConsumer");
        dataConsumerCheckBox.setEnabled(false);
        JCheckBox dataManipulatorCheckBox = new JCheckBox("DataManipulator");
        dataManipulatorCheckBox.setEnabled(false);
        JCheckBox dataPublisherCheckBox = new JCheckBox("DataPublisher");
        dataPublisherCheckBox.setEnabled(false);
        JCheckBox uiProviderCheckBox = new JCheckBox("UIProvider");
        uiProviderCheckBox.setEnabled(false);
        componentBehaviourPanel.add(dataConsumerCheckBox);
        componentBehaviourPanel.add(dataManipulatorCheckBox);
        componentBehaviourPanel.add(dataPublisherCheckBox);
        componentBehaviourPanel.add(uiProviderCheckBox);

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
            pluginComponentList.setListData(
                    pluginComponents.get(selectedIndex)
                            .stream()
                            .map(x -> x.getClass().getName())
                            .toArray(String[]::new)
            );
        });

        pluginComponentList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int selectedIndex = pluginComponentList.getSelectedIndex();
            if (selectedIndex == -1) {
                return;
            }

            List<Object> selectedPluginComponents = pluginComponents.get(pluginList.getSelectedIndex());
            Object selectedPluginComponent = selectedPluginComponents.get(selectedIndex);
            dataConsumerCheckBox.setSelected(selectedPluginComponent instanceof DataConsumer);
            dataManipulatorCheckBox.setSelected(selectedPluginComponent instanceof DataManipulator);
            dataPublisherCheckBox.setSelected(selectedPluginComponent instanceof DataPublisher);
            uiProviderCheckBox.setSelected(selectedPluginComponent instanceof UIProvider);
        });

        panel.add(pluginListPanel);
        panel.add(pluginDetailPanel);
        panel.add(pluginComponentListPanel);
        panel.add(componentBehaviourPanel);

        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.setContentPane(panel);
        this.setSize(640, 640);
        this.setResizable(false);
    }
}
