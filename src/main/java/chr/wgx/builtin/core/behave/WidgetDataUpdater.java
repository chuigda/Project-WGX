package chr.wgx.builtin.core.behave;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPluginBehavior;

public final class WidgetDataUpdater implements IPluginBehavior {
    @Override
    public String name() {
        return "WGC_WidgetDataUpdater";
    }

    @Override
    public String description() {
        return "用于同步 Reactor 中的数据和 UI 上实际显示的数据";
    }

    @Override
    public int priority() {
        return 7000;
    }

    @Override
    public void tick(Reactor reactor) throws Exception {
    }
}
