package chr.wgx.reactor;

import tech.icey.xjbutil.functional.Action1;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

public final class Radioactive<T> {
    private final AtomicReference<T> value;
    private final HashSet<Action1<T>> listeners = new HashSet<>();

    public Radioactive(T initValue) {
        value = new AtomicReference<>(initValue);
    }

    public T get() {
        return value.get();
    }

    public void set(T newValue) {
        value.set(newValue);
        new Thread(() -> {
            synchronized (listeners) {
                listeners.forEach(listener -> listener.apply(newValue));
            }
        }).start();
    }

    public void addListener(Action1<T> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Action1<T> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
}
