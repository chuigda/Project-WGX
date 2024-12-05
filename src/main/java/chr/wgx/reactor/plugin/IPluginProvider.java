package chr.wgx.reactor.plugin;

import chr.wgx.reactor.Reactor;

public interface IPluginProvider {
    String name();
    String description();

    int initPriority();
    IPlugin init(Reactor reactor) throws Exception;
}
