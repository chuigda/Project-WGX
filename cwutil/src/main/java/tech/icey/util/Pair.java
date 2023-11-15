package tech.icey.util;

import java.util.Objects;

public class Pair<T1, T2> {
    public T1 first;
    public T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair<?, ?> other) {
            return Objects.equals(first, other.first) &&
                    Objects.equals(second, other.second);
        }
        return false;
    }
}
