package chr.wgx.builtin.core.behave;

import chr.wgx.builtin.BehaviorPriorities;
import chr.wgx.builtin.core.WGCCommon;
import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPluginBehavior;
import tech.icey.xjbutil.container.Ref;

public final class StateInitializer implements IPluginBehavior {
    public StateInitializer(Ref<Boolean> coreDataProgramUpdated, Ref<Boolean> cameraConfigProgramUpdated) {
        this.coreDataProgramUpdated = coreDataProgramUpdated;
        this.cameraConfigProgramUpdated = cameraConfigProgramUpdated;
    }

    @Override
    public String name() {
        return "WGC_StateInitializer";
    }

    @Override
    public String description() {
        return "用于初始化 " + WGCCommon.class.getCanonicalName() + " 插件的状态变量";
    }

    @Override
    public int priority() {
        return BehaviorPriorities.CORE_STATE_INITIALIZER;
    }

    @Override
    public void tick(Reactor reactor) {
        coreDataProgramUpdated.value = false;
        cameraConfigProgramUpdated.value = false;
    }

    private final Ref<Boolean> coreDataProgramUpdated;
    private final Ref<Boolean> cameraConfigProgramUpdated;
}
