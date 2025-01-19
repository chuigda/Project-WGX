package chr.wgx.reactor.plugin;

import chr.wgx.reactor.Reactor;

public interface IPluginBehavior extends Comparable<IPluginBehavior> {
    String name();
    String description();
    int priority();

    void tick(Reactor reactor) throws Exception;

    /// 下游插件行为无论如何都不应该重写此方法。下游插件应重写 {@link IPluginBehavior#priority} 并提供正确的优先级。
    /// 内建插件行为的优先级可于 {@link chr.wgx.builtin.BehaviorPriorities} 查询。
    @Override
    default int compareTo(IPluginBehavior o) {
        return Integer.compare(priority(), o.priority());
    }
}
