package chr.wgx.builtin.osf;

import chr.wgx.builtin.BehaviorPriorities;
import chr.wgx.builtin.core.data.CoreData;
import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPluginBehavior;

public final class DataUpdater implements IPluginBehavior {
    @Override
    public String name() {
        return "WGC_OSF_DataUpdater";
    }

    @Override
    public String description() {
        return "将 OpenSeeFace 收集到的数据上传到 " + CoreData.class.getCanonicalName() + " 中";
    }

    @Override
    public int priority() {
        return BehaviorPriorities.OSF_DATA_UPDATER;
    }

    @Override
    public void tick(Reactor reactor) {

    }
}
