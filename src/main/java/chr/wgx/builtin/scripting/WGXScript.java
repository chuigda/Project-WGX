package chr.wgx.builtin.scripting;

import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPlugin;

public class WGXScript implements IPlugin {
    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void tick(Reactor reactor) throws Exception {

    }
}
