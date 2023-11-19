package tech.icey.wgx;

import tech.icey.r77.Init;
import tech.icey.r77.vk.Instance;
import tech.icey.r77.vk.VkWindow;
import tech.icey.util.Logger;

import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("发生了一个错误，观感设置将不可用，回退到默认观感");
        }
        new ControlWindow().setVisible(true);

        Logger.setLevel(Logger.Level.DEBUG);
        Init.initialise();

        Instance instance = new Instance("Project-WGX", true);

        try (var window = new VkWindow("你好", 600, 600)) {
            while (window.poll()) {}
        }
    }
}
