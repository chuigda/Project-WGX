package chr.wgx.ui.config;

import chr.wgx.render.vk.VulkanConfig;
import chr.wgx.ui.SwingUtil;
import tech.icey.xjbutil.container.Option;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class VulkanConfigWidget extends JPanel {
    public VulkanConfigWidget(List<VulkanDeviceInfo> deviceInfoList) {
        super();

        this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Vulkan 配置"),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        this.setLayout(new GridBagLayout());

        SwingUtil.addConfigItemFast(
                this,
                0,
                "物理设备 ID",
                physicalDeviceSelectionButton,
                "在系统安装有有多个支持 Vulkan 的物理设备时，指定使用的物理设备 ID\n" +
                "若不指定使用的物理设备，则会自动选择找到的第一个支持 Vulkan 的物理设备"
        );
        SwingUtil.addConfigItemFast(
                this,
                1,
                "Vulkan 校验层",
                validationLayersComboBox,
                "启用 Vulkan 校验层 VK_LAYER_KHRONOS_validation 以追踪错误和其他问题\n" +
                "会少许降低性能，建议只在调试和创建错误报告时启用"
        );
        vsyncComboBox.setSelectedIndex(VulkanConfig.DEFAULT.vsync);
        SwingUtil.addConfigItemFast(this, 2, "垂直同步", vsyncComboBox);
        SwingUtil.addConfigItemFast(this, 3, "最大 FPS", maxFPSField);
        SwingUtil.addConfigItemFast(
                this,
                4,
                "最大同时渲染帧数",
                maxFramesInFlightField,
                "允许渲染器同时渲染多少帧，设置过大的值可能导致延迟"
        );
        SwingUtil.addConfigItemFast(this, 5, "多重采样抗锯齿", enableMSAAComboBox);
        SwingUtil.addConfigItemFast(this, 6, "多重采样样本数", msaaSampleCountField);
        SwingUtil.addConfigItemFast(this, 7, "各向异性过滤", enableAnisotropyComboBox);
        SwingUtil.addConfigItemFast(this, 8, "各向异性过滤级别", anisotropyLevelField);
        SwingUtil.addConfigItemFast(
                this,
                9,
                "强制 UNORM 格式",
                forceUNORMComboBox,
                "强制为所有图像使用 UNORM 格式而非 SRGB 格式\n" +
                "在某些 AMD 显卡上启用，可以解决启用 MSAA 时场景整体偏暗的问题"
        );
        SwingUtil.addConfigItemFast(
                this,
                10,
                "强制以图形队列上传",
                alwaysUploadWithGraphicsQueueComboBox,
                "注意: 开发人员选项\n" +
                "即使图形处理器支持专用传输队列，也强制使用图形队列上传所有数据"
        );

        physicalDeviceSelectionButton.addActionListener(_ -> {
            VulkanDeviceSelectDialog dialog = new VulkanDeviceSelectDialog(
                    (JFrame) this.getTopLevelAncestor(),
                    deviceInfoList,
                    currentConfig.physicalDeviceID,
                    info -> {
                        if (info instanceof Option.Some<VulkanDeviceInfo> someInfo) {
                            physicalDeviceSelectionButton.setText(Integer.toString(someInfo.value.deviceId));
                            currentConfig.physicalDeviceID = someInfo.value.deviceId;
                        } else {
                            physicalDeviceSelectionButton.setText("未指定");
                            currentConfig.physicalDeviceID = 0;
                        }
                    }
            );
            dialog.setVisible(true);
        });
    }

    final JButton physicalDeviceSelectionButton = new JButton("未指定");
    final JComboBox<String> validationLayersComboBox = new JComboBox<>(new String[]{ "关闭", "启用" });
    final JComboBox<String> vsyncComboBox = new JComboBox<>(new String[]{
            "禁用垂直同步",
            "偏好不使用垂直同步",
            "强制启用垂直同步"
    });
    final JTextField maxFPSField = new JTextField(Integer.toString(VulkanConfig.DEFAULT.maxFPS));
    final JTextField maxFramesInFlightField = new JTextField(Integer.toString(VulkanConfig.DEFAULT.maxFramesInFlight));
    final JComboBox<String> enableMSAAComboBox = new JComboBox<>(new String[]{ "关闭", "启用" });
    final JTextField msaaSampleCountField = new JTextField(Integer.toString(VulkanConfig.DEFAULT.msaaSampleCount));
    final JComboBox<String> enableAnisotropyComboBox = new JComboBox<>(new String[]{ "关闭", "启用" });
    final JTextField anisotropyLevelField = new JTextField(Float.toString(VulkanConfig.DEFAULT.anisotropyLevel));
    final JComboBox<String> forceUNORMComboBox = new JComboBox<>(new String[]{ "关闭", "启用" });
    final JComboBox<String> alwaysUploadWithGraphicsQueueComboBox = new JComboBox<>(new String[]{ "关闭", "启用" });

    final VulkanConfig currentConfig = new VulkanConfig();
}
