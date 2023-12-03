package tech.icey.wgx;

import tech.icey.r77.vk.PhysicalDeviceProperties;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DeviceInfoWindow extends JFrame {
    public DeviceInfoWindow(List<PhysicalDeviceProperties> physicalDeviceProperties) {
        super("选择设备");

        JPanel innerPane = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        innerPane.setLayout(layout);

        JLabel deviceLabel = new JLabel("选择设备:");
        JComboBox<String> comboBox = new JComboBox<>(new String[]{
                "DEVICE_A",
                "DEVICE_B",
                "DEVICE_C"
        });

        JTextArea detailTextArea = new JTextArea();
        detailTextArea.setEditable(false);
        detailTextArea.setLineWrap(true);
        detailTextArea.setWrapStyleWord(true);
        detailTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        detailTextArea.setText("hihihi I am here");

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
