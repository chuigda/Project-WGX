package tech.icey.util;

public class RadioactiveRef<T> implements Radioactive {
    public RadioactiveRef(T value) {
        this.value = value;
        // make this true to make sure the first time after creation, the value looks dirty so any "dependents" will
        // read the value
        this.dirty = true;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        this.dirty = true;
    }

    @Override
    public boolean dirty() {
        return dirty;
    }

    @Override
    public void clearDirty() {
        dirty = false;
    }

    private T value;
    private boolean dirty;
}
