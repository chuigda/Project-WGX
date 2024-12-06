package chr.wgx.reactor.plugin;

import chr.wgx.reactor.Reactor;

public interface IPluginFactory {
    String name();
    String description();

    int initPriority();
    IPlugin create(Reactor reactor) throws Exception;
}
