package chr.wgx.builtin.scripting;

import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginBehavior;

import java.util.List;

public class WGXScript implements IPlugin {
    @Override
    public List<IPluginBehavior> behaviors() {
        return List.of();
    }
}
