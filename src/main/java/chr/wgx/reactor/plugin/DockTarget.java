package chr.wgx.reactor.plugin;

public sealed class DockTarget permits WidgetDockTarget, MenuDockTarget {
    public final String targetName;
    public final long location;

    public DockTarget(String targetName, long location) {
        this.targetName = targetName;
        this.location = location;
    }
}
