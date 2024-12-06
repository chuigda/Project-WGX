package chr.wgx.reactor.plugin;

import chr.wgx.reactor.Reactor;

public interface IPluginBehavior {
    String name();
    String description();
    int priority();

    void tick(Reactor reactor) throws Exception;
}
