package chr.wgx.reactor.plugin;

import chr.wgx.reactor.IWidget;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public interface IWidgetProvider extends IPlugin {
    sealed class DockTarget permits WidgetDockTarget, MenuDockTarget {
        public final String targetName;
        public final long location;

        public DockTarget(String targetName, long location) {
            this.targetName = targetName;
            this.location = location;
        }
    }

    final class WidgetDockTarget extends DockTarget {
        public WidgetDockTarget(String targetName, long location) {
            super(targetName, location);
        }
    }

    final class MenuDockTarget extends DockTarget {
        public MenuDockTarget(String targetName, long location) {
            super(targetName, location);
        }
    }

    List<Pair<DockTarget, IWidget>> provide();
}
