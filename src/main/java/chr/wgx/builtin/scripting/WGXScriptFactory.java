package chr.wgx.builtin.scripting;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginFactory;

public final class WGXScriptFactory implements IPluginFactory {
    @Override
    public String name() {
        return "";
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public IPlugin create(Reactor reactor) throws Exception {
        return null;
    }
}
