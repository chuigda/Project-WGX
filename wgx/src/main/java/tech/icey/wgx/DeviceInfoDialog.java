package tech.icey.wgx;

import tech.icey.r77.vk.PhysicalDeviceProperties;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DeviceInfoDialog extends JDialog {
    public DeviceInfoDialog(
            List<PhysicalDeviceProperties> physicalDeviceProperties,
            JFrame owner
    ) {
        super(owner, "选择设备", true);

        JPanel innerPane = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        innerPane.setLayout(layout);

        JLabel deviceLabel = new JLabel("选择设备:");
        JComboBox<String> comboBox = new JComboBox<>(
                physicalDeviceProperties.stream()
                        .map(PhysicalDeviceProperties::deviceName)
                        .toArray(String[]::new)
        );

        JTextArea detailTextArea = new JTextArea();
        detailTextArea.setEditable(false);
        detailTextArea.setLineWrap(true);
        detailTextArea.setWrapStyleWord(true);
        detailTextArea.setFont(FontDatabase.defaultMonospaceFont);
        detailTextArea.setText("hihihi I am here 我阐释你的梦");
        detailTextArea.setBorder(BorderFactory.createLineBorder(UIManager.getColor("MenuBar.borderColor"), 1));

        JButton cancelButton = new JButton("取消");
        JButton okButton = new JButton("确定");

        {
            GridBagConstraints deviceLabelConstraints = new GridBagConstraints();
            deviceLabelConstraints.gridx = 0;
            deviceLabelConstraints.gridy = 0;
            deviceLabelConstraints.weightx = 0;
            deviceLabelConstraints.weighty = 0;
            deviceLabelConstraints.fill = GridBagConstraints.NONE;
            deviceLabelConstraints.anchor = GridBagConstraints.WEST;
            deviceLabelConstraints.insets = new Insets(0, 0, 0, 4);
            innerPane.add(deviceLabel, deviceLabelConstraints);
        }

        {
            GridBagConstraints comboBoxConstraints = new GridBagConstraints();
            comboBoxConstraints.gridx = 1;
            comboBoxConstraints.gridy = 0;
            comboBoxConstraints.weightx = 1;
            comboBoxConstraints.weighty = 0;
            comboBoxConstraints.fill = GridBagConstraints.HORIZONTAL;
            comboBoxConstraints.anchor = GridBagConstraints.EAST;
            innerPane.add(comboBox, comboBoxConstraints);
        }

        {
            GridBagConstraints detailTextAreaConstraints = new GridBagConstraints();
            detailTextAreaConstraints.gridx = 0;
            detailTextAreaConstraints.gridy = 2;
            detailTextAreaConstraints.gridwidth = 2;
            detailTextAreaConstraints.weightx = 1;
            detailTextAreaConstraints.weighty = 1;
            detailTextAreaConstraints.fill = GridBagConstraints.BOTH;
            detailTextAreaConstraints.anchor = GridBagConstraints.WEST;
            detailTextAreaConstraints.insets = new Insets(4, 0, 4, 0);
            innerPane.add(detailTextArea, detailTextAreaConstraints);
        }

        {
            GridBagConstraints cancelButtonConstraints = new GridBagConstraints();
            cancelButtonConstraints.gridx = 0;
            cancelButtonConstraints.gridy = 3;
            cancelButtonConstraints.weightx = 0;
            cancelButtonConstraints.weighty = 0;
            cancelButtonConstraints.fill = GridBagConstraints.NONE;
            cancelButtonConstraints.anchor = GridBagConstraints.WEST;
            innerPane.add(cancelButton, cancelButtonConstraints);
        }

        {
            GridBagConstraints okButtonConstraints = new GridBagConstraints();
            okButtonConstraints.gridx = 1;
            okButtonConstraints.gridy = 3;
            okButtonConstraints.weightx = 0;
            okButtonConstraints.weighty = 0;
            okButtonConstraints.fill = GridBagConstraints.NONE;
            okButtonConstraints.anchor = GridBagConstraints.EAST;
            innerPane.add(okButton, okButtonConstraints);
        }

        innerPane.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.setContentPane(innerPane);

        this.setSize(480, 360);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
