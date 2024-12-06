package chr.wgx.reactor;

import chr.wgx.render.AbstractRenderEngine;

import java.util.HashMap;

public final class Reactor {
    public final AbstractRenderEngine renderEngine;
    public final HashMap<String, Object> constantPool = new HashMap<>();
    public final HashMap<String, Object> volatilePool = new HashMap<>();
    public final HashMap<String, Radioactive> radioactivePool = new HashMap<>();

    public Reactor(AbstractRenderEngine renderEngine) {
        this.renderEngine = renderEngine;
    }

    public void mainLoop() {}
}
