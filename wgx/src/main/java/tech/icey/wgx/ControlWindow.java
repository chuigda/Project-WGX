package tech.icey.wgx;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public final class ControlWindow extends JFrame {
    public ControlWindow() {
        super("Project-WGX - 控制器");
        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon-v2.png")));
        this.setIconImage(icon.getImage());

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        this.setSize(500, 100);
        this.setResizable(false);
        GridLayout mainLayout = new GridLayout(2, 5, 2, 2);
        contentPanel.setLayout(mainLayout);

        contentPanel.add(new JButton("系统信息"));
        contentPanel.add(new JButton("位置控制"));
        contentPanel.add(new JButton("姿态控制"));
        contentPanel.add(new JButton("关节动画"));
        contentPanel.add(new JButton("附加配件"));
        contentPanel.add(new JButton("屏幕画面"));
        contentPanel.add(new JButton("音频分析"));
        contentPanel.add(new JButton("附加选项"));
        contentPanel.add(new JButton("插件管理"));
        contentPanel.add(new JButton("关于"));

        this.setContentPane(contentPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
