package chr.wgx.render.task;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RenderTask {
    public final AtomicBoolean enabled = new AtomicBoolean(true);

    protected RenderTask() {}

    public final void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public final boolean isEnabled() {
        return this.enabled.get();
    }
}
