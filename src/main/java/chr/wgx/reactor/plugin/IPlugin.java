package chr.wgx.reactor.plugin;

import chr.wgx.reactor.Reactor;

public interface IPlugin {
    default String className() {
        return this.getClass().getName();
    }

    int priority();
    void tick(Reactor reactor) throws Exception;
}
