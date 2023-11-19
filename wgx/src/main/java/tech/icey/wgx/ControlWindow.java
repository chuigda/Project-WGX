package tech.icey.wgx;

import javax.swing.*;
import java.awt.*;

public final class ControlWindow extends JFrame {
    public ControlWindow() {
        super("Project-WGX - Control Window");
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        this.setSize(500, 100);
        this.setResizable(false);
        GridLayout mainLayout = new GridLayout(2, 5, 2, 2);
        contentPanel.setLayout(mainLayout);

        contentPanel.add(new JButton("系统信息"));
        contentPanel.add(new JButton("姿态控制"));
        contentPanel.add(new JButton("关节动画"));
        contentPanel.add(new JButton("屏幕画面"));
        contentPanel.add(new JButton("音频分析"));
        contentPanel.add(new JButton(""));
        contentPanel.add(new JButton(""));
        contentPanel.add(new JButton(""));
        contentPanel.add(new JButton(""));
        contentPanel.add(new JButton(""));

        this.setContentPane(contentPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
