package chr.wgx.builtin.wgcv2;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPluginFactory;

public final class WGXFactory implements IPluginFactory {
    @Override
    public String name() {
        return "WGX";
    }

    @Override
    public String description() {
        return "新版本的 WorkGroup Commons 机器人外观";
    }

    @Override
    public WGX create(Reactor reactor) throws Exception {
        return new WGX();
    }
}
