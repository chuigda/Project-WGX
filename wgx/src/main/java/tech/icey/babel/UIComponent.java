package tech.icey.babel;

import tech.icey.util.Logger;

import javax.swing.*;

public abstract sealed class UIComponent {
    public abstract Object getUnderlyingItem();

    public static final class MenuItem extends UIComponent {
        public final JFrame frame;

        public final String menuName;
        public final String menuItemName;
        public final String popupFrameName;

        public MenuItem(JFrame frame, String menuName, String menuItemName, String popupFrameName) {
            if (frame.getDefaultCloseOperation() != JFrame.HIDE_ON_CLOSE) {
                logger.log(
                        Logger.Level.WARN,
                        "插件窗口 %s (具有类型 %s) 的行为不是 JFrame.HIDE_ON_CLOSE。插件系统会尝试修复这个问题。",
                        frame.getTitle(),
                        frame.getClass().getName()
                );
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            }

            this.frame = frame;
            this.menuName = menuName;
            this.menuItemName = menuItemName;
            this.popupFrameName = popupFrameName;
        }

        @Override
        public Object getUnderlyingItem() {
            return frame;
        }

        private static final Logger logger = new Logger(MenuItem.class.getName());
    }

    public static final class SubElement extends UIComponent {
        public final JPanel panel;

        public final String name;
        public final String targetName;
        public final long location;

        public SubElement(JPanel panel, String name, String targetName, long location) {
            this.panel = panel;
            this.name = name;
            this.targetName = targetName;
            this.location = location;
        }

        @Override
        public Object getUnderlyingItem() {
            return panel;
        }
    }
}
