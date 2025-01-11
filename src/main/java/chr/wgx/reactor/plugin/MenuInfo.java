package chr.wgx.reactor.plugin;

import tech.icey.xjbutil.container.Option;

public final class MenuInfo {
    public final Option<String> parent;
    public final String name;
    public final String displayName;
    public final int sortingOrder;

    public MenuInfo(Option<String> parent, String name, String displayName, int sortingOrder) {
        this.parent = parent;
        this.name = name;
        this.displayName = displayName;
        this.sortingOrder = sortingOrder;
    }

    public MenuInfo(String name, String displayName, int sortingOrder) {
        this(Option.none(), name, displayName, sortingOrder);
    }

    public MenuInfo(String parent, String name, String displayName, int sortingOrder) {
        this(Option.some(parent), name, displayName, sortingOrder);
    }
}
