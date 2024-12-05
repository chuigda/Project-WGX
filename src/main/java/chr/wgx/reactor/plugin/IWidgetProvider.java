package chr.wgx.reactor.plugin;

import chr.wgx.reactor.IWidget;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public interface IWidgetProvider extends IPlugin {
    class DockTarget {
        public final String className;
        public final long location;

        public DockTarget(String className, long location) {
            this.className = className;
            this.location = location;
        }
    }

    List<Pair<DockTarget, IWidget>> provide();
}
