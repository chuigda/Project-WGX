package chr.wgx.ui.config;

import chr.wgx.ui.SwingUtil;
import club.doki7.ffm.annotation.unsigned;
import club.doki7.vulkan.enumtype.VkPhysicalDeviceType;
import tech.icey.xjbutil.container.Option;
import tech.icey.xjbutil.functional.Action1;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class VulkanDeviceSelectDialog extends JDialog {
    public VulkanDeviceSelectDialog(
            JFrame parent,
            List<VulkanDeviceInfo> deviceInfoList,
            @Unsigned int initialSelectedDeviceId,
            Action1<Option<VulkanDeviceInfo>> onDeviceSelected
    ) {
        super(parent, "选择 Vulkan 设备", true);

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagLayout verticalLayout = new GridBagLayout();
        contentPanel.setLayout(verticalLayout);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        deviceNameComboBox = new JComboBox<>();
        deviceNameComboBox.addItem("未指定 - 运行时自动选取");
        for (VulkanDeviceInfo deviceInfo : deviceInfoList) {
            deviceNameComboBox.addItem(deviceInfo.deviceName);
        }
        c.gridy = 0;
        contentPanel.add(deviceNameComboBox, c);

        if (initialSelectedDeviceId != 0) {
            boolean validInitialSelectedDeviceId = false;

            for (int i = 0; i < deviceInfoList.size(); i++) {
                if (deviceInfoList.get(i).deviceId == initialSelectedDeviceId) {
                    deviceNameComboBox.setSelectedIndex(i + 1);
                    validInitialSelectedDeviceId = true;
                    break;
                }
            }

            if (!validInitialSelectedDeviceId) {
                JOptionPane.showMessageDialog(
                        parent,
                        "未找到指定的 Vulkan 设备 ID: " + initialSelectedDeviceId,
                        "错误",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        deviceInfoTextArea = new JTextArea();
        deviceInfoTextArea.setEditable(false);
        deviceInfoTextArea.setLineWrap(true);
        deviceInfoTextArea.setBackground(Color.WHITE);
        SwingUtil.createTextAreaMenu(deviceInfoTextArea);
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 1;
        c.weighty = 1;
        contentPanel.add(new JScrollPane(deviceInfoTextArea), c);

        deviceNameComboBox.addActionListener(e -> {
            int index = deviceNameComboBox.getSelectedIndex();
            if (index == 0) {
                deviceInfoTextArea.setText("未指定 - 运行时自动选取");
                onDeviceSelected.apply(Option.none());
            } else {
                VulkanDeviceInfo deviceInfo = deviceInfoList.get(index - 1);
                deviceInfoTextArea.setText(
                        "设备 ID: " + deviceInfo.deviceId + "\n" +
                        "设备名称: " + deviceInfo.deviceName + "\n" +
                        "设备类型: " + switch (deviceInfo.deviceType) {
                            case VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU -> "集成 GPU";
                            case VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU -> "独立 GPU";
                            case VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU -> "虚拟 GPU";
                            case VkPhysicalDeviceType.VK_PHYSICAL_DEVICE_TYPE_CPU -> "CPU";
                            default -> "未知";
                        } + "\n" +
                        "Vulkan API 版本: " + deviceInfo.apiVersion.major() + "." + deviceInfo.apiVersion.minor() + "." + deviceInfo.apiVersion.patch() + "\n" +
                        "多重采样抗锯齿: " + (deviceInfo.supportsMSAA ? "是" : "否") + "\n" +
                        "多重采样抗锯齿样本数: " + deviceInfo.msaaSampleCounts + "\n" +
                        "最大各向异性过滤: " + deviceInfo.maxAnisotropy + "\n" +
                        "专用传输队列: " + (deviceInfo.dedicatedTransferQueue ? "是" : "否")
                );
                onDeviceSelected.apply(Option.some(deviceInfo));
            }
        });

        this.setContentPane(contentPanel);
        this.pack();
        this.setSize(480, 360);
        this.setResizable(false);
        this.setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private final JComboBox<String> deviceNameComboBox;
    private final JTextArea deviceInfoTextArea;
}
