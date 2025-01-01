package chr.wgx.builtin.core;

import chr.wgx.reactor.IWidget;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginBehavior;
import chr.wgx.reactor.plugin.IWidgetProvider;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public class WGCCommon implements IPlugin, IWidgetProvider {
    @Override
    public List<IPluginBehavior> behaviors() {
        return List.of();
    }

    @Override
    public List<Pair<DockTarget, IWidget>> provide() {
        return List.of();
    }
}
