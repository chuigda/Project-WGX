package chr.wgx.util;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Radioactive {
    private final AtomicBoolean isDirty = new AtomicBoolean(true);

    public boolean isDirty() {
        return isDirty.get();
    }

    public void setDirty() {
        isDirty.set(true);
    }

    public void clearDirty() {
        isDirty.set(false);
    }

    public boolean getAndClearDirty() {
        return isDirty.getAndSet(false);
    }
}
