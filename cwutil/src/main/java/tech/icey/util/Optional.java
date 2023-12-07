package tech.icey.util;

import static tech.icey.util.RuntimeError.runtimeError;

public abstract sealed class Optional<T> {
    public static final class Some<T> extends Optional<T> {
        public final T value;

        public Some(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value.toString();
        }
    }

    public static final class None<T> extends Optional<T> {
        @Override
        public String toString() {
            return "None";
        }
    }

    public static <T> Optional<T> some(T value) {
        if (value == null) {
            runtimeError("非法调用: Optional.some(null)");
        }
        return new Some<>(value);
    }

    public static <T> Optional<T> none() {
        return new None<>();
    }

    public static <T> Optional<T> fromNullable(T value) {
        if (value == null) {
            return none();
        } else {
            return some(value);
        }
    }

    public T get() {
        return ((Some<T>)this).value;
    }

    public boolean isSome() {
        return this instanceof Some;
    }

    public boolean isNone() {
        return this instanceof None;
    }

    public T getOrDefault(T defaultValue) {
        if (this instanceof Some) {
            return ((Some<T>)this).value;
        } else {
            return defaultValue;
        }
    }

    public T getOrCompute(Function0<T> supplier) {
        if (this instanceof Some) {
            return ((Some<T>)this).value;
        } else {
            return supplier.apply();
        }
    }
}
