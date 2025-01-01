package chr.wgx.reactor;

import chr.wgx.render.RenderEngine;
import tech.icey.xjbutil.container.Pair;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    public static void startReactor(RenderEngine engine) {
        new Thread(() -> reactorMain(engine)).start();
    }

    private static void reactorMain(RenderEngine engine) {
        Reactor reactor = new Reactor(engine);

        while (true) {
            long startTime = System.nanoTime();

            // logics here
            if (reactor.framebufferSizeChanged.getAndSet(false)) {
                reactor.framebufferWidth = reactor.framebufferSize.get().first();
                reactor.framebufferHeight = reactor.framebufferSize.get().second();
                reactor.framebufferResized = true;
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
}
