package chr.wgx.reactor.plugin;

import chr.wgx.reactor.IWidget;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public interface IWidgetProvider extends IPlugin {
    List<Pair<DockTarget, IWidget>> provide();
}
