package chr.wgx.builtin.core;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginFactory;

public final class WGCCommonFactory implements IPluginFactory {
    @Override
    public String name() {
        return "WGC 通用组件";
    }

    @Override
    public String description() {
        return "WGC 系列机器人通用的数据和 UI 组件";
    }

    @Override
    public int initPriority() {
        return -1000;
    }

    @Override
    public IPlugin create(Reactor reactor) throws Exception {
        return new WGCCommon();
    }
}
