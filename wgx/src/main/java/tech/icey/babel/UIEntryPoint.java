package tech.icey.babel;

public abstract sealed class UIEntryPoint {
    public static final class MenuItem extends UIEntryPoint {
        public final String menuName;
        public final String menuItemName;
        public final String popupFrameName;

        public MenuItem(String menuName, String menuItemName, String popupFrameName) {
            this.menuName = menuName;
            this.menuItemName = menuItemName;
            this.popupFrameName = popupFrameName;
        }
    }

    public static final class SubElement extends UIEntryPoint {
        public final String name;
        public final String targetName;
        public final long location;

        public SubElement(String name, String targetName, long location) {
            this.name = name;
            this.targetName = targetName;
            this.location = location;
        }
    }
}