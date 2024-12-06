package chr.wgx.builtin.wgcv1;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPluginFactory;

public final class WGCV1Factory implements IPluginFactory {
    @Override
    public String name() {
        return "WGC-0310 v1.0";
    }

    @Override
    public String description() {
        return "旧版本的 WGC-310";
    }

    @Override
    public int initPriority() {
        return -1000;
    }

    @Override
    public WGCV1 create(Reactor reactor) throws Exception {
        return new WGCV1();
    }
}
