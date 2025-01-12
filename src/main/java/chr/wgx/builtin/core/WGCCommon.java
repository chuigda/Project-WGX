package chr.wgx.builtin.core;

import chr.wgx.builtin.core.behave.Averager;
import chr.wgx.builtin.core.behave.StateInitializer;
import chr.wgx.builtin.core.behave.WidgetDataUpdater;
import chr.wgx.builtin.core.data.CameraConfig;
import chr.wgx.builtin.core.data.CoreData;
import chr.wgx.builtin.core.widget.CameraConfigWidget;
import chr.wgx.reactor.IWidget;
import chr.wgx.reactor.Radioactive;
import chr.wgx.reactor.Reactor;
import chr.wgx.reactor.plugin.*;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;

import java.util.List;

public class WGCCommon implements IPlugin, IWidgetProvider, IMenuProvider {
    WGCCommon(Reactor reactor) {
        CoreData coreData = new CoreData();
        Ref<Boolean> coreDataProgramUpdated = new Ref<>(false);

        Radioactive<CameraConfig> cameraConfig = new Radioactive<>(new CameraConfig());
        Ref<Boolean> cameraConfigProgramUpdated = new Ref<>(false);

        Ref<Boolean> averagerEnabled = new Ref<>(true);
        Ref<Integer> averagerFrameCount = new Ref<>(3);

        reactor.volatilePool.put("WGC_CoreData", coreData);
        reactor.volatilePool.put("WGC_CoreData_ProgramUpdated", coreDataProgramUpdated);

        reactor.radioactivePool.put("WGC_CameraConfig", cameraConfig);
        reactor.volatilePool.put("WGC_CameraConfig_ProgramUpdated", cameraConfigProgramUpdated);

        reactor.volatilePool.put("WGC_Averager_Enabled", averagerEnabled);
        reactor.volatilePool.put("WGC_Averager_FrameCount", averagerFrameCount);

        cameraConfigWidget = new CameraConfigWidget();

        stateInitializer = new StateInitializer(coreDataProgramUpdated, cameraConfigProgramUpdated);
        averager = new Averager(coreData, coreDataProgramUpdated, averagerEnabled, averagerFrameCount);
        widgetDataUpdater = new WidgetDataUpdater(cameraConfig, cameraConfigProgramUpdated, cameraConfigWidget);
    }

    @Override
    public List<IPluginBehavior> behaviors() {
        return List.of(stateInitializer, averager, widgetDataUpdater);
    }

    @Override
    public List<Pair<DockTarget, IWidget>> provide() {
        return List.of(new Pair<>(new MenuDockTarget("WGC_Menu_Control", 0), cameraConfigWidget));
    }

    @Override
    public List<MenuInfo> provideMenu() {
        return List.of(new MenuInfo("WGC_Menu_Control", "控制", 0));
    }

    private final CameraConfigWidget cameraConfigWidget;

    private final StateInitializer stateInitializer;
    private final Averager averager;
    private final WidgetDataUpdater widgetDataUpdater;
}
