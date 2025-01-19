package chr.wgx.builtin.osf;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginFactory;

public final class OpenSeeFaceFactory implements IPluginFactory {
    @Override
    public String name() {
        return "OpenSeeFace 数据接收器";
    }

    @Override
    public String description() {
        return "从 OpenSeeFace 接收面捕数据并用于驱动 WGC-0310";
    }

    @Override
    public IPlugin create(Reactor reactor) throws Exception {
        return new OpenSeeFace();
    }
}
