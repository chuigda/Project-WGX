package chr.wgx.reactor.plugin;

import chr.wgx.reactor.IWidget;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public interface IWidgetProvider extends IPlugin {
    class DockTarget {
        public final String targetName;
        public final long location;

        public DockTarget(String targetName, long location) {
            this.targetName = targetName;
            this.location = location;
        }
    }

    List<Pair<DockTarget, IWidget>> provide();
}
