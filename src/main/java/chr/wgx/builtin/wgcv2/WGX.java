package chr.wgx.builtin.wgcv2;

import chr.wgx.reactor.IWidget;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IWidgetProvider;
import tech.icey.xjbutil.container.Pair;

import java.util.List;

public final class WGX implements IPlugin, IWidgetProvider {
    @Override
    public List<Pair<DockTarget, IWidget>> provide() {
        return List.of();
    }
}
