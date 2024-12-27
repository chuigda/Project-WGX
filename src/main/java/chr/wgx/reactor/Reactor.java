package chr.wgx.reactor;

import chr.wgx.render.RenderEngine;

import java.util.HashMap;

public final class Reactor {
    public final RenderEngine renderEngine;

    /// 提交给 Reactor 之后就不会修改的稳定对象
    public final HashMap<String, Object> stablePool = new HashMap<>();
    /// 每一 tick 都必然发生变更的挥发性对象
    public final HashMap<String, Object> volatilePool = new HashMap<>();
    /// 变更时机不定，并且需要监视的放射性对象
    public final HashMap<String, Radioactive<?>> radioactivePool = new HashMap<>();

    public Reactor(RenderEngine renderEngine) {
        this.renderEngine = renderEngine;
    }

    public void mainLoop() {}
}
