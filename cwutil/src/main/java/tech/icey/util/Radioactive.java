package tech.icey.util;

public final class Radioactive<T> {
    public Radioactive(T value) {
        this.value = value;
        this.dirty = false;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        this.dirty = true;
    }

    public void setDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    private T value;
    private boolean dirty;
}
