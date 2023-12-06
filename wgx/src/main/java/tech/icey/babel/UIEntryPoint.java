package tech.icey.babel;

public abstract sealed class UIEntryPoint {}

final class ContextMenu extends UIEntryPoint {
    public final String menuName;
    public final String menuItemName;
    public final Runnable action;

    public ContextMenu(String menuName, String menuItemName, Runnable action) {
        this.menuName = menuName;
        this.menuItemName = menuItemName;
        this.action = action;
    }
}

final class SubElement extends UIEntryPoint {
    public final String name;
    public final String targetName;
    public final long location;

    public SubElement(String name, String targetName, long location) {
        this.name = name;
        this.targetName = targetName;
        this.location = location;
    }
}
