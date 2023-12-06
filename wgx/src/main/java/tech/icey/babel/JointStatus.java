package tech.icey.babel;

import tech.icey.util.Radioactive;

public final class JointStatus implements Radioactive {
    @Override
    public boolean dirty() {
        return false;
    }

    @Override
    public void clearDirty() {
    }
}
