package chr.wgx.reactor.plugin;

import chr.wgx.reactor.Reactor;

public interface IPluginFactory {
    String name();
    String description();

    IPlugin create(Reactor reactor) throws Exception;
}
