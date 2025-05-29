package chr.wgx.reactor;

import chr.wgx.builtin.core.WGCCommonFactory;
import chr.wgx.builtin.wgcv1.WGCV1Factory;
import chr.wgx.drill.DrillPlugin;
import chr.wgx.reactor.plugin.*;
import chr.wgx.render.RenderEngine;
import chr.wgx.ui.ControlWindow;
import chr.wgx.ui.ProgressDialog;
import tech.icey.xjbutil.container.Pair;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public final class Reactor {
    public final RenderEngine renderEngine;

    public int framebufferWidth;
    public int framebufferHeight;
    public boolean framebufferResized;

    /// 提交给 Reactor 之后就不会修改的稳定对象
    public final HashMap<String, Object> stablePool = new HashMap<>();
    /// 每一 tick 都必然发生变更的挥发性对象
    public final HashMap<String, Object> volatilePool = new HashMap<>();
    /// 变更时机不定，并且需要监视的放射性对象
    public final HashMap<String, Radioactive<?>> radioactivePool = new HashMap<>();

    public Reactor(RenderEngine renderEngine) {
        this.renderEngine = renderEngine;

        Pair<Integer, Integer> framebufferSize = renderEngine.framebufferSize();

        this.framebufferWidth = framebufferSize.first();
        this.framebufferHeight = framebufferSize.second();
        this.framebufferResized = false;

        this.framebufferSize = new AtomicReference<>(framebufferSize);
        this.framebufferSizeChanged = new AtomicBoolean(false);
        renderEngine.onResizeActions.add((width, height) -> {
            this.framebufferSize.set(new Pair<>(width, height));
            this.framebufferSizeChanged.set(true);
        });
    }

    private final AtomicReference<Pair<Integer, Integer>> framebufferSize;
    private final AtomicBoolean framebufferSizeChanged;

    public static void reactorMain(
            RenderEngine renderEngine,
            ControlWindow controlWindow,
            ProgressDialog progressDlg
    ) {
        Reactor reactor = new Reactor(renderEngine);

        // TODO make this configurable, or maybe construct this list somewhere else
        List<IPluginFactory> pluginFactoryList = List.of(
                new WGCCommonFactory(),
                new WGCV1Factory()
                // , new DrillPlugin.Factory()
        );

        int perPluginProgress = (100 - 35) / pluginFactoryList.size();
        SortedSet<IPluginBehavior> pluginBehaviors = new TreeSet<>();
        List<MenuInfo> menuInfos = new ArrayList<>();
        List<Pair<DockTarget, IWidget>> widgets = new ArrayList<>();

        int currentProgress = 35;
        for (IPluginFactory factory : pluginFactoryList) {
            progressDlg.setProgress(currentProgress, "初始化插件 " + factory.name());
            try {
                IPlugin plugin = factory.create(reactor);
                logger.info("插件 " + factory.name() + " (" + plugin.className() + ") 初始化成功");

                for (IPluginBehavior behavior : plugin.behaviors()) {
                    progressDlg.setProgress("注册插件行为 " + behavior.name());
                    pluginBehaviors.add(behavior);
                    logger.info(
                            "插件 " + factory.name() + " 注册行为 " + behavior.name()
                                    + " (" + behavior.getClass().getCanonicalName() + ")"
                    );
                }

                if (plugin instanceof IMenuProvider menuProvider) {
                    for (MenuInfo menuInfo : menuProvider.provideMenu()) {
                        progressDlg.setProgress("注册菜单项 " + menuInfo.name);
                        menuInfos.add(menuInfo);
                        logger.info("插件 " + factory.name() + " 注册菜单项 " + menuInfo.name);
                    }
                }

                if (plugin instanceof IWidgetProvider widgetProvider) {
                    for (Pair<DockTarget, IWidget> pair : widgetProvider.provide()) {
                        progressDlg.setProgress("注册小部件 " + pair.second().displayName());
                        widgets.add(pair);
                        logger.info(
                                "插件 " + factory.name() + " 注册小部件 " + pair.second().displayName()
                                        + " (" + pair.second().getClass().getCanonicalName() + ")"
                        );
                    }
                }
            } catch (Throwable e) {
                logger.severe("无法初始化插件 " + factory.name() + ": " + e.getMessage());
                e.printStackTrace();
            }

            currentProgress += perPluginProgress;
        }

        controlWindow.addWidgets(menuInfos, widgets);

        logger.info(() -> {
            StringBuilder sb = new StringBuilder();
            sb.append("已注册的插件行为: ");
            for (IPluginBehavior behavior : pluginBehaviors) {
                sb.append("\n\t- [")
                        .append(behavior.priority())
                        .append("] ")
                        .append(behavior.name())
                        .append(" (")
                        .append(behavior.getClass().getCanonicalName())
                        .append(")");
            }
            return sb.toString();
        });

        progressDlg.setProgress(100, "插件系统初始化完成");
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(1000);
                progressDlg.dispose();
            } catch (InterruptedException _) {
                // do nothing
            }
        });

        //noinspection InfiniteLoopStatement
        while (true) {
            long startTime = System.nanoTime();

            // logics here
            if (reactor.framebufferSizeChanged.getAndSet(false)) {
                reactor.framebufferWidth = reactor.framebufferSize.get().first();
                reactor.framebufferHeight = reactor.framebufferSize.get().second();
                reactor.framebufferResized = true;
            }

            for (IPluginBehavior behavior : pluginBehaviors) {
                try {
                    behavior.tick(reactor);
                } catch (Throwable e) {
                    logger.severe(
                            "行为 " + behavior.name()
                                    + "("
                                    + behavior.getClass().getCanonicalName()
                                    + ") 执行时发生异常: "
                                    + e.getMessage()
                    );
                }
            }

            // finalize
            reactor.framebufferResized = false;
            for (Radioactive<?> radioactive : reactor.radioactivePool.values()) {
                radioactive.changed = false;
            }

            long endTime = System.nanoTime();
            long timeToSleep = 16_666_666 - (endTime - startTime);
            try {
                //noinspection BusyWait
                Thread.sleep(timeToSleep / 1_000_000, (int) (timeToSleep % 1_000_000));
            } catch (InterruptedException _) {
                // Do nothing
            }
        }
    }

    private static final Logger logger = Logger.getLogger(Reactor.class.getName());
}
