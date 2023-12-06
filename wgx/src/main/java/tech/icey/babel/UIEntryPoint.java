package tech.icey.babel;

public abstract sealed class UIEntryPoint {}

final class ContextMenu extends UIEntryPoint {
    public final String name;
    public final Runnable action;

    public ContextMenu(String name, Runnable action) {
        this.name = name;
        this.action = action;
    }
}

final class SubElement extends UIEntryPoint {
    public final String name;
    public final Class<? extends DockingPort> target;
    public final long location;

    public SubElement(String name, Class<? extends DockingPort> target, long location) {
        this.name = name;
        this.target = target;
        this.location = location;
    }
}
