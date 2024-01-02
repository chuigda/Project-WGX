package tech.icey.r77.vk;

public final class GraphicsQueue extends Queue {
    @Override
    public boolean isManuallyDisposed() {
        return isDisposed;
    }

    @Override
    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
        }
    }

    private volatile boolean isDisposed = false;
}
