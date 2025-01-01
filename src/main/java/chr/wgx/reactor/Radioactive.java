package chr.wgx.reactor;

public final class Radioactive<T> {
    public T value;
    public boolean changed = false;

    public Radioactive(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        this.changed = true;
    }
}
