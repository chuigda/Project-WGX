package chr.wgx.reactor;

import chr.wgx.render.AbstractRenderEngine;

public final class Reactor {
    public final AbstractRenderEngine renderEngine;

    public Reactor(AbstractRenderEngine renderEngine) {
        this.renderEngine = renderEngine;
    }

    public void mainLoop() {}
}
