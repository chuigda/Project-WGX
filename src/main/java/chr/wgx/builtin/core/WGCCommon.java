package chr.wgx.builtin.core;

import chr.wgx.builtin.core.behave.Averager;
import chr.wgx.builtin.core.behave.StateInitializer;
import chr.wgx.builtin.core.data.CameraConfig;
import chr.wgx.builtin.core.data.CoreData;
import chr.wgx.reactor.IWidget;
import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.IPlugin;
import chr.wgx.reactor.plugin.IPluginBehavior;
import chr.wgx.reactor.plugin.IWidgetProvider;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;

import java.util.List;

public class WGCCommon implements IPlugin, IWidgetProvider {
    WGCCommon(Reactor reactor) {
        CoreData coreData = new CoreData();
        Ref<Boolean> coreDataProgramUpdated = new Ref<>(false);

        CameraConfig cameraConfig = new CameraConfig(reactor);
        Ref<Boolean> cameraConfigProgramUpdated = new Ref<>(false);

        Ref<Boolean> averagerEnabled = new Ref<>(true);
        Ref<Integer> averagerFrameCount = new Ref<>(3);

        reactor.volatilePool.put("WGC_CoreData", coreData);
        reactor.volatilePool.put("WGC_CoreData_ProgramUpdated", coreDataProgramUpdated);

        reactor.stablePool.put("WGC_CameraConfig", cameraConfig);
        reactor.stablePool.put("WGC_CameraConfig_ProgramUpdated", cameraConfigProgramUpdated);

        reactor.volatilePool.put("WGC_Averager_Enabled", averagerEnabled);
        reactor.volatilePool.put("WGC_Averager_FrameCount", averagerFrameCount);

        stateInitializer = new StateInitializer(coreDataProgramUpdated, cameraConfigProgramUpdated);
        averager = new Averager(coreData, coreDataProgramUpdated, averagerEnabled, averagerFrameCount);
    }

    @Override
    public List<IPluginBehavior> behaviors() {
        return List.of(stateInitializer, averager);
    }

    @Override
    public List<Pair<DockTarget, IWidget>> provide() {
        return List.of();
    }

    private final StateInitializer stateInitializer;
    private final Averager averager;
}
