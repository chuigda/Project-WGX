package chr.wgx.reactor.plugin;

import chr.wgx.reactor.Reactor;

public interface IPluginBehavior extends Comparable<IPluginBehavior> {
    String name();
    String description();
    int priority();

    void tick(Reactor reactor) throws Exception;

    @Override
    default int compareTo(IPluginBehavior o) {
        return Integer.compare(priority(), o.priority());
    }
}
