package chr.wgx.reactor.plugin;

import java.util.List;

public interface IPlugin {
    default String className() {
        return this.getClass().getName();
    }

    default List<IPluginBehavior> behaviors() {
        return List.of();
    }
}
